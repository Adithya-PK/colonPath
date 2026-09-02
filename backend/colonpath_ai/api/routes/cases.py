import re
from pathlib import Path
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, status
from fastapi.responses import FileResponse
from api.schemas import CaseResultResponse, CaseSummaryItem
from api.services.case_service import CaseService
from api.exceptions import ValidationError, CaseNotFoundError
from system_config import config

router = APIRouter(prefix="/cases", tags=["Cases"])
case_service = CaseService()


def validate_case_id(case_id: str) -> str:
    cleaned = case_id.strip()
    if not re.match(r"^[a-zA-Z0-9_\-\.]+$", cleaned) or ".." in cleaned:
        raise ValidationError(
            message=f"Invalid Case ID '{case_id}'. Path traversal and special characters are forbidden."
        )
    return cleaned


@router.get("", response_model=List[CaseSummaryItem])
def list_cases(limit: int = 50):
    cases = case_service.list_cases(limit=limit)
    return cases


@router.get("/{case_id}", response_model=Dict[str, Any])
def get_case_meta(case_id: str):
    cid = validate_case_id(case_id)
    meta = case_service.get_case_meta(cid)
    if not meta:
        raise CaseNotFoundError(case_id=cid)
    return meta


@router.get("/{case_id}/result", response_model=CaseResultResponse)
def get_case_result(case_id: str):
    cid = validate_case_id(case_id)
    result = case_service.get_case_result(cid)
    if not result:
        raise CaseNotFoundError(case_id=cid)
    return result


@router.get("/{case_id}/image")
def get_case_image(case_id: str):
    cid = validate_case_id(case_id)
    meta = case_service.get_case_meta(cid)
    if not meta or not meta.get("image_path"):
        raise CaseNotFoundError(case_id=cid)
    img_path = Path(meta["image_path"])
    if not img_path.exists():
        raise CaseNotFoundError(case_id=cid)
    return FileResponse(img_path)


@router.get("/{case_id}/visualization/{vis_type}")
def get_case_visualization(case_id: str, vis_type: str):
    cid = validate_case_id(case_id)
    vtype = vis_type.lower().strip()
    if vtype not in config.allowed_visualizations:
        raise ValidationError(
            message=f"Invalid visualization type '{vis_type}'. Allowed types: {config.allowed_visualizations}"
        )

    result = case_service.get_case_result(cid)
    if not result:
        raise CaseNotFoundError(case_id=cid)

    candidates = [
        config.cases_dir / cid / "visualizations" / f"{vtype}.png",
        config.output_dir / "visualizations" / cid / f"{vtype}.png",
        config.cases_dir / cid / "cv" / "unet" / "gland_mask.png" if vtype == "glands" else None,
        config.cases_dir / cid / "cv" / "hovernet" / "nuclei_overlay.png" if vtype == "nuclei" else None,
    ]

    for cand in candidates:
        if cand and cand.exists():
            return FileResponse(str(cand), media_type="image/png")

    raise CaseNotFoundError(case_id=f"{cid} (vis: {vtype})")


@router.get("/{case_id}/evidence", response_model=Dict[str, Any])
def get_case_evidence(case_id: str):
    """
    Returns the deterministic computational evidence payload (evidence.json) for a case.
    """
    cid = validate_case_id(case_id)
    result = case_service.get_case_result(cid)
    if not result:
        raise CaseNotFoundError(case_id=cid)

    evidence = {
        "case_id": result.get("case_id"),
        "prediction": result.get("prediction"),
        "uncertainty": result.get("uncertainty"),
        "model_agreement": result.get("model_agreement"),
        "nuclear_evidence": result.get("nuclear_evidence"),
        "gland_evidence": result.get("gland_evidence"),
        "priority_regions": result.get("priority_regions"),
        "explanation": result.get("explanation"),
        "model_performance_metadata": result.get("model_performance_metadata"),
        "reproducibility": result.get("reproducibility"),
    }
    return evidence


@router.post("/{case_id}/review", status_code=status.HTTP_200_OK)
def review_case(case_id: str, review_data: Dict[str, Any]):
    cid = validate_case_id(case_id)
    result = case_service.get_case_result(cid)
    if not result:
        raise CaseNotFoundError(case_id=cid)

    action = review_data.get("action", "MARK_REVIEWED")
    notes = review_data.get("notes", "")
    pathologist_id = review_data.get("pathologist_id", "Dr. Pathologist")

    case_service.add_review(cid, action=action, notes=notes, pathologist_id=pathologist_id)
    return {
        "status": "SUCCESS",
        "case_id": cid,
        "review_action": action,
        "message": f"Review record logged for case '{cid}'."
    }
