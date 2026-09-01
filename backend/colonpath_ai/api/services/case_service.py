"""
Case Service Layer for Managing Analysis and Case Queries.
"""

from pathlib import Path
from typing import Optional, List, Dict, Any
from storage.case_repository import CaseRepository
from orchestrator.pipeline import CaseOrchestrator

PROJECT_ROOT = Path(__file__).resolve().parents[2]


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
    ) -> Dict[str, Any]:
        """
        Dynamically executes full CV pipeline (U-Net, HoVer-Net, Morphometry)
        on the uploaded image and passes case-isolated artifacts to orchestrator.
        """
        cid = case_id or image_path.stem
        case_output_dir = PROJECT_ROOT / "outputs" / "cases" / cid
        case_output_dir.mkdir(parents=True, exist_ok=True)

        # Import unified CV pipeline runner dynamically
        import sys
        WORKSPACE_ROOT = PROJECT_ROOT.parent.parent if PROJECT_ROOT.name == "colonpath_ai" else PROJECT_ROOT.parent
        cv_dir = WORKSPACE_ROOT / "cv"
        if str(cv_dir) not in sys.path:
            sys.path.insert(0, str(cv_dir))

        try:
            from cv_pipeline import run_cv_pipeline
            cv_res = run_cv_pipeline(image_path=image_path, case_id=cid, output_dir=case_output_dir)
            artifacts = cv_res.get("artifacts", {})
            nuclei_csv = Path(artifacts["nuclei_csv_path"])
            glands_csv = Path(artifacts["glands_csv_path"])
            gland_mask = Path(artifacts["gland_mask_path"])
            nuclei_overlay = Path(artifacts["nuclei_overlay_path"])
        except Exception as e:
            raise RuntimeError(f"Dynamic Computer Vision execution failed for case '{cid}': {str(e)}")

        result = self.orchestrator.run(
            image_path=image_path,
            case_id=cid,
            nuclei_csv=nuclei_csv,
            glands_csv=glands_csv,
            gland_mask_path=gland_mask,
            nuclei_overlay_path=nuclei_overlay,
        )
        return result

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
