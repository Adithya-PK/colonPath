"""
Case Service Layer for Managing Analysis, Lifecycle State, Idempotency, and Case Queries.
"""

import sys
import shutil
import logging
from pathlib import Path
from typing import Optional, List, Dict, Any, Set
from datetime import datetime, timezone

from storage.case_repository import CaseRepository
from orchestrator.pipeline import CaseOrchestrator, CaseStage
from api.exceptions import CaseAlreadyProcessingError, PipelineExecutionError

logger = logging.getLogger("colonpath_case_service")
PROJECT_ROOT = Path(__file__).resolve().parents[2]

# In-memory active processing set for concurrency / idempotency protection
_ACTIVE_PROCESSING_CASES: Set[str] = set()


class CaseService:
    def __init__(
        self,
        repository: Optional[CaseRepository] = None,
        orchestrator: Optional[CaseOrchestrator] = None,
    ):
        self.repository = repository or CaseRepository()
        self.orchestrator = orchestrator or CaseOrchestrator(repository=self.repository)

    def analyze_image(
        self,
        image_path: Path,
        case_id: Optional[str] = None,
        force_reanalyze: bool = False,
    ) -> Dict[str, Any]:
        """
        Executes full CV and Post-CV pipeline with lifecycle validation,
        idempotency protection, and transient artifact cleanup.
        """
        cid = case_id or image_path.stem

        # 1. Idempotency Check: Already in progress?
        if cid in _ACTIVE_PROCESSING_CASES:
            logger.warning(f"[IDEMPOTENCY] Case '{cid}' is currently processing. Rejecting duplicate request.")
            raise CaseAlreadyProcessingError(case_id=cid)

        # Always execute live inference for active requests to ensure 100% dynamic metrics per image
        _ACTIVE_PROCESSING_CASES.add(cid)
        case_output_dir = PROJECT_ROOT / "outputs" / "cases" / cid
        case_output_dir.mkdir(parents=True, exist_ok=True)

        WORKSPACE_ROOT = PROJECT_ROOT.parent.parent if PROJECT_ROOT.name == "colonpath_ai" else PROJECT_ROOT.parent
        cv_dir = WORKSPACE_ROOT / "cv"
        if str(cv_dir) not in sys.path:
            sys.path.insert(0, str(cv_dir))

        temp_dirs_to_clean: List[Path] = []
        try:
            logger.info(f"[STAGE: {CaseStage.CV_PROCESSING.value}] Executing Computer Vision Pipeline for case '{cid}'")
            from cv_pipeline import run_cv_pipeline
            cv_res = run_cv_pipeline(image_path=image_path, case_id=cid, output_dir=case_output_dir)
            artifacts = cv_res.get("artifacts", {})
            nuclei_csv = Path(artifacts["nuclei_csv_path"])
            glands_csv = Path(artifacts["glands_csv_path"])
            gland_mask = Path(artifacts["gland_mask_path"])
            nuclei_overlay = Path(artifacts["nuclei_overlay_path"])

            # Run Orchestrator Pipeline
            result = self.orchestrator.run(
                image_path=image_path,
                case_id=cid,
                nuclei_csv=nuclei_csv,
                glands_csv=glands_csv,
                gland_mask_path=gland_mask,
                nuclei_overlay_path=nuclei_overlay,
            )
            return result

        except Exception as e:
            logger.error(f"[CASE_SERVICE_ERROR] Case '{cid}' analysis failed: {str(e)}")
            if isinstance(e, PipelineExecutionError):
                raise e
            raise PipelineExecutionError(message=str(e), case_id=cid, stage=CaseStage.FAILED.value, retryable=True)

        finally:
            # Always remove from active processing set
            _ACTIVE_PROCESSING_CASES.discard(cid)
            
            # Safe cleanup of transient files if any exist
            for td in temp_dirs_to_clean:
                if td.exists() and td.is_dir():
                    try:
                        shutil.rmtree(td, ignore_errors=True)
                        logger.info(f"[CLEANUP] Removed transient directory: {td}")
                    except Exception as ce:
                        logger.warning(f"[CLEANUP_WARN] Could not clean {td}: {ce}")

    def get_case_result(self, case_id: str) -> Optional[Dict[str, Any]]:
        return self.repository.get_case_result(case_id)

    def get_case_meta(self, case_id: str) -> Optional[Dict[str, Any]]:
        return self.repository.get_case(case_id)

    def list_cases(self, limit: int = 50) -> List[Dict[str, Any]]:
        return self.repository.list_cases(limit=limit)

    def add_review(self, case_id: str, action: str, notes: str, pathologist_id: str) -> None:
        self.repository.add_review(case_id, action, notes, pathologist_id)

    def add_note(self, case_id: str, note_text: str, author: str) -> None:
        self.repository.add_note(case_id, note_text, author)

    def get_notes(self, case_id: str) -> List[Dict[str, Any]]:
        return self.repository.get_notes(case_id)
