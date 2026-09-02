"""
Health Check API Route with Dynamic Model Readiness Verification.
"""

from pathlib import Path
from fastapi import APIRouter
import torch
from api.schemas import HealthResponse
from foundation.phikon.model_loader import FoundationModelLoader

router = APIRouter(tags=["Health"])

BACKEND_ROOT = Path(__file__).resolve().parents[2]
WORKSPACE_ROOT = Path(__file__).resolve().parents[4]


@router.get("/health", response_model=HealthResponse)
def health_check():
    device = "cuda" if torch.cuda.is_available() else "cpu"

    # Verify real checkpoint files on disk
    unet_ckpt = WORKSPACE_ROOT / "cv" / "outputs" / "unet" / "best_model.pth"
    hovernet_ckpt = (
        WORKSPACE_ROOT
        / "cv"
        / "models"
        / "hovernet"
        / "checkpoints"
        / "hovernet_original_consep_type_tf2pytorch"
    )
    hovernet_ref_ckpt = (
        WORKSPACE_ROOT
        / "cv"
        / "hovernet_reference"
        / "checkpoints"
        / "hovernet_original_consep_type_tf2pytorch"
    )
    classifier_ckpt = BACKEND_ROOT / "outputs" / "models" / "best_classifier.pth"

    unet_ready = unet_ckpt.exists()
    hovernet_ready = hovernet_ckpt.exists() or hovernet_ref_ckpt.exists()
    classifier_ready = classifier_ckpt.exists()

    # Dynamic foundation readiness check
    try:
        loader = FoundationModelLoader.get_instance(device=device)
        foundation_ready = loader.is_ready()
    except Exception:
        foundation_ready = False

    all_ready = unet_ready and hovernet_ready and foundation_ready and classifier_ready

    return HealthResponse(
        status="healthy",
        service="COLONPATH-AI Multimodal Backend",
        version="2.0.0",
        device=device,
        models_ready=all_ready,
    )
