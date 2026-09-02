"""
Analysis API Route for Full H&E Histopathology Evaluation.
"""

import re
import shutil
import logging
from pathlib import Path
from typing import Optional
from fastapi import APIRouter, UploadFile, File, Form, status
from api.schemas import CaseResultResponse
from api.services.case_service import CaseService
from api.exceptions import ValidationError, PipelineExecutionError, ColonPathException
from system_config import config

logger = logging.getLogger("colonpath_analysis_route")

router = APIRouter(tags=["Analysis"])
case_service = CaseService()

UPLOAD_DIR = config.uploads_dir
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


@router.post("/analyze", response_model=CaseResultResponse, status_code=status.HTTP_200_OK)
async def analyze_image(
    image: UploadFile = File(..., description="H&E Histopathology image (PNG, JPG, BMP, TIF)"),
    case_id: Optional[str] = Form(None, description="Optional Case Identifier"),
    force_reanalyze: Optional[bool] = Form(False, description="Whether to re-execute completed case"),
):
    if not image.filename:
        raise ValidationError(message="Uploaded file must have a valid filename.")

    # 1. Validate file extension
    file_ext = Path(image.filename).suffix.lower() or ".png"
    if file_ext not in config.allowed_extensions:
        raise ValidationError(
            message=f"Unsupported image format '{file_ext}'. Allowed formats: {config.allowed_extensions}"
        )

    # 2. Validate and sanitize Case ID against path traversal
    raw_cid = case_id.strip() if case_id and case_id.strip() else Path(image.filename).stem
    if not re.match(r"^[a-zA-Z0-9_\-\.]+$", raw_cid) or ".." in raw_cid:
        raise ValidationError(
            message="Invalid Case ID. Must contain only alphanumeric characters, underscores, dashes, or dots without path traversal."
        )
    safe_cid = raw_cid

    # 3. Save uploaded file
    save_path = UPLOAD_DIR / f"{safe_cid}{file_ext}"
    try:
        with open(save_path, "wb") as buffer:
            shutil.copyfileobj(image.file, buffer)
    except Exception as e:
        raise PipelineExecutionError(
            message="Failed to write uploaded image stream to disk storage.",
            case_id=safe_cid,
            stage="PREPROCESSING",
            retryable=True,
        )

    # 4. Check file size
    file_size_mb = save_path.stat().st_size / (1024 * 1024)
    if file_size_mb > config.max_upload_size_mb:
        save_path.unlink(missing_ok=True)
        raise ValidationError(
            message=f"File size ({file_size_mb:.1f} MB) exceeds maximum allowed limit of {config.max_upload_size_mb} MB.",
            case_id=safe_cid,
            stage="VALIDATING",
        )

    logger.info(f"[STAGE: IMAGE_ACCEPTED] Case '{safe_cid}', file: {save_path.name} ({file_size_mb:.2f} MB)")

    # 5. Execute pipeline
    try:
        result = case_service.analyze_image(
            image_path=save_path,
            case_id=safe_cid,
            force_reanalyze=force_reanalyze or False,
        )
        logger.info(f"[STAGE: CASE_COMPLETED] Case '{safe_cid}' successfully processed.")
        return result
    except ColonPathException as ce:
        raise ce
    except Exception as e:
        logger.error(f"[STAGE: CASE_FAILED] Case '{safe_cid}' failed: {e}")
        raise PipelineExecutionError(
            message=str(e),
            case_id=safe_cid,
            stage="PROCESSING",
            retryable=True,
        )
