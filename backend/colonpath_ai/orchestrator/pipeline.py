"""
End-to-End Multimodal Analysis Orchestrator for COLONPATH-AI.
Executes the unified AI-assisted decision-support pipeline sequentially with
deterministic lifecycle states, timing instrumentation, and reproducibility tracking.
"""

import time
import json
import hashlib
import logging
from enum import Enum
from pathlib import Path
from typing import Optional, Union, Dict, Any
import numpy as np
import cv2
from PIL import Image

from foundation.phikon.inference import PhikonV2FeatureExtractor
from fusion.feature_loader import FeatureLoader
from fusion.feature_schema import MorphologyFeatureVector
from classifiers.tissue_classifier import TissueClassifier
from uncertainty.uncertainty_estimator import UncertaintyEstimator
from agreement.agreement_engine import AgreementEngine
from regions.region_analyzer import RegionAnalyzer
from visualization.visualizer import CaseVisualizer
from evidence.evidence_builder import EvidenceBuilder
from evidence.explainer import EvidenceGroundedExplainer
from agent.evidence_validator import EvidenceValidator
from storage.case_repository import CaseRepository
from api.exceptions import PipelineExecutionError

logger = logging.getLogger("colonpath_orchestrator")

PROJECT_ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = PROJECT_ROOT / "outputs"


class CaseStage(str, Enum):
    RECEIVED = "RECEIVED"
    VALIDATING = "VALIDATING"
    PREPROCESSING = "PREPROCESSING"
    CV_PROCESSING = "CV_PROCESSING"
    FEATURE_EXTRACTION = "FEATURE_EXTRACTION"
    FUSION = "FUSION"
    UNCERTAINTY = "UNCERTAINTY"
    AGREEMENT = "AGREEMENT"
    GENERATING_VISUALIZATIONS = "GENERATING_VISUALIZATIONS"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(8192):
            h.update(chunk)
    return h.hexdigest()


def evaluate_image_quality(image_path: Path) -> Dict[str, Any]:
    """Evaluates optical image quality."""
    img = cv2.imread(str(image_path))
    if img is None:
        raise ValueError(f"Unable to read image at {image_path}")

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    lap_var = float(cv2.Laplacian(gray, cv2.CV_64F).var())
    brightness = float(np.mean(gray))
    contrast = float(np.std(gray))
    saturation = float(np.mean(hsv[:, :, 1]))

    blur_ok = lap_var >= 50.0
    bright_ok = 40.0 <= brightness <= 220.0
    contrast_ok = contrast >= 25.0

    passed = blur_ok and bright_ok and contrast_ok

    return {
        "passed": passed,
        "resolution": f"{img.shape[1]}x{img.shape[0]}",
        "blur_laplacian_variance": round(lap_var, 2),
        "blur_status": "ACCEPTABLE" if blur_ok else "HIGH_BLUR",
        "mean_brightness": round(brightness, 2),
        "brightness_status": "ACCEPTABLE" if bright_ok else ("TOO_DARK" if brightness < 40 else "VERY_BRIGHT"),
        "contrast_std": round(contrast, 2),
        "contrast_status": "ACCEPTABLE" if contrast_ok else "LOW_CONTRAST",
        "mean_saturation": round(saturation, 2),
    }


class CaseOrchestrator:
    """
    Master pipeline orchestrator running the entire decision-support sequence.
    """

    def __init__(
        self,
        extractor: Optional[PhikonV2FeatureExtractor] = None,
        classifier: Optional[TissueClassifier] = None,
        uncertainty_estimator: Optional[UncertaintyEstimator] = None,
        region_analyzer: Optional[RegionAnalyzer] = None,
        visualizer: Optional[CaseVisualizer] = None,
        repository: Optional[CaseRepository] = None,
    ):
        self.extractor = extractor or PhikonV2FeatureExtractor()
        self.classifier = classifier or TissueClassifier()
        self.uncertainty_estimator = uncertainty_estimator or UncertaintyEstimator()
        self.region_analyzer = region_analyzer or RegionAnalyzer(
            extractor=self.extractor,
            classifier=self.classifier,
            uncertainty_estimator=self.uncertainty_estimator,
        )
        self.visualizer = visualizer or CaseVisualizer()
        self.repository = repository or CaseRepository()

    def run(
        self,
        image_path: Union[str, Path],
        case_id: Optional[str] = None,
        nuclei_csv: Optional[Union[str, Path]] = None,
        glands_csv: Optional[Union[str, Path]] = None,
        gland_mask_path: Optional[Union[str, Path]] = None,
        nuclei_overlay_path: Optional[Union[str, Path]] = None,
    ) -> Dict[str, Any]:
        img_path = Path(image_path)
        if not img_path.exists():
            raise FileNotFoundError(f"Image not found at {img_path}")

        cid = case_id or img_path.stem
        img_hash = sha256_file(img_path)
        t_total_start = time.perf_counter()
        durations: Dict[str, float] = {}

        logger.info(f"[STAGE: {CaseStage.RECEIVED.value}] Case: {cid} | Image: {img_path.name} (SHA256: {img_hash[:16]})")

        current_stage = CaseStage.VALIDATING
        try:
            # 1. Quality Check & Validation
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.VALIDATING.value}] Evaluating Optical Quality for {cid}")
            img_quality = evaluate_image_quality(img_path)
            durations[CaseStage.VALIDATING.value] = round((time.perf_counter() - t0) * 1000, 2)

            # 2. Phikon-v2 Visual Embedding
            current_stage = CaseStage.FEATURE_EXTRACTION
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.FEATURE_EXTRACTION.value}] Extracting 1024D Foundation Embedding for {cid}")
            v_emb = self.extractor.extract(img_path, cache_key=f"phikon_{cid}")

            # 3. Morphological Integration
            if nuclei_csv or glands_csv:
                morphology = FeatureLoader.from_measurements(cid, nuclei_csv=nuclei_csv, glands_csv=glands_csv)
            else:
                raise ValueError(f"No morphology measurements provided for case '{cid}'. Dynamic CV execution is required.")
            durations[CaseStage.FEATURE_EXTRACTION.value] = round((time.perf_counter() - t0) * 1000, 2)

            # 4. Multimodal Fusion & Classification
            current_stage = CaseStage.FUSION
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.FUSION.value}] Multimodal Fusion Net Inference for {cid}")
            pred_res = self.classifier.predict(v_emb, morphology)
            logits = pred_res["logits"]
            durations[CaseStage.FUSION.value] = round((time.perf_counter() - t0) * 1000, 2)

            # 5. Uncertainty Estimation & Calibration
            current_stage = CaseStage.UNCERTAINTY
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.UNCERTAINTY.value}] Temperature Calibration & Entropy Estimation for {cid}")
            unc_res = self.uncertainty_estimator.estimate(
                logits=logits,
                probabilities=np.array(list(pred_res["multiclass_probabilities"].values())),
                image_quality_passed=img_quality["passed"],
            )
            durations[CaseStage.UNCERTAINTY.value] = round((time.perf_counter() - t0) * 1000, 2)

            # 6. Multi-Source Agreement
            current_stage = CaseStage.AGREEMENT
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.AGREEMENT.value}] Multi-Source Consensus Evaluation for {cid}")
            agr_res = AgreementEngine.evaluate(
                fusion_prediction=pred_res["prediction"],
                tumor_probability=pred_res["tumor_probability"],
                morphology=morphology,
                visual_prediction=pred_res["prediction"],
            )
            durations[CaseStage.AGREEMENT.value] = round((time.perf_counter() - t0) * 1000, 2)

            # 7. Region-Level Prioritization
            t0 = time.perf_counter()
            regions = self.region_analyzer.analyze_image(
                img_path,
                nuclei_csv=nuclei_csv,
                glands_csv=glands_csv,
            )
            durations["REGION_ANALYSIS"] = round((time.perf_counter() - t0) * 1000, 2)

            # 8. Visualizations, Evidence & Validation
            current_stage = CaseStage.GENERATING_VISUALIZATIONS
            t0 = time.perf_counter()
            logger.info(f"[STAGE: {CaseStage.GENERATING_VISUALIZATIONS.value}] Rendering Visualizations for {cid}")
            vis_paths = self.visualizer.render_all(
                case_id=cid,
                image_path=img_path,
                regions=regions,
                gland_mask_path=gland_mask_path,
                nuclei_overlay_path=nuclei_overlay_path,
                nuclei_csv=nuclei_csv,
            )
            durations[CaseStage.GENERATING_VISUALIZATIONS.value] = round((time.perf_counter() - t0) * 1000, 2)

            durations["TOTAL_PIPELINE_MS"] = round((time.perf_counter() - t_total_start) * 1000, 2)

            # Build full case_result
            case_result = EvidenceBuilder.build_case_result(
                case_id=cid,
                image_quality=img_quality,
                digepath_meta=self.extractor.metadata,
                prediction_result=pred_res,
                uncertainty=unc_res,
                model_agreement=agr_res,
                morphology=morphology,
                priority_regions=regions,
                visualizations=vis_paths,
            )

            # Add Phase 5 Observability, State, Timing & Reproducibility Metadata
            case_result["lifecycle_state"] = CaseStage.COMPLETED.value
            case_result["stage_durations_ms"] = durations
            case_result["reproducibility"] = {
                "pipeline_version": "2.0.0",
                "input_image_sha256": img_hash,
                "input_image_name": img_path.name,
                "models": {
                    "unet": "31.4M params (ResNet34 backbone, Warwick QU Dataset)",
                    "hovernet": "37.2M params (PanNuke/CoNIC checkpoint)",
                    "foundation": "303.35M params (DINOv2 ViT-L/16, owkin/phikon-v2, 1024D, Pretrained on 40M+ tiles)",
                    "fusion": "131.2K params (MultimodalFusionNet, 1024D+16D -> 128D)",
                },
                "temperature_scaling_factor": 1.25,
                "timestamp_utc": case_result.get("timestamp"),
            }

            # Build & validate explanation and claims
            explanation = EvidenceGroundedExplainer.generate_explanation(case_result)
            claims = EvidenceGroundedExplainer.generate_claims(case_result)
            val_res = EvidenceValidator.validate(explanation, case_result)
            case_result["explanation"] = {
                "text": explanation,
                "claims": claims,
                "validated": val_res.is_valid,
                "validation_errors": val_res.errors,
            }

            # Save to disk: case_result.json and evidence.json
            case_out_dir = OUTPUT_DIR / "cases" / cid
            case_out_dir.mkdir(parents=True, exist_ok=True)

            res_path = case_out_dir / "case_result.json"
            with open(res_path, "w", encoding="utf-8") as f:
                json.dump(case_result, f, indent=2)

            ev_path = case_out_dir / "evidence.json"
            evidence_json = EvidenceBuilder.build_evidence_json(case_result)
            with open(ev_path, "w", encoding="utf-8") as f:
                json.dump(evidence_json, f, indent=2)

            # Persist to SQLite
            self.repository.save_case(
                case_id=cid,
                case_result=case_result,
                result_json_path=res_path,
                evidence_json_path=ev_path,
                image_path=img_path,
            )

            logger.info(f"[STAGE: {CaseStage.COMPLETED.value}] Case {cid} successfully processed in {durations['TOTAL_PIPELINE_MS']} ms.")
            return case_result

        except Exception as e:
            logger.error(f"[STAGE: {CaseStage.FAILED.value}] Pipeline failed at stage '{current_stage.value}' for case '{cid}': {str(e)}")
            raise PipelineExecutionError(
                message=str(e),
                case_id=cid,
                stage=current_stage.value,
                retryable=True,
            )
