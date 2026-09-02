"""
COLONPATH-AI Centralized Configuration Engine.
Manages runtime environment settings, paths, device allocation, and model weights configuration.
"""

import os
from pathlib import Path
from typing import Optional
from pydantic import BaseModel, Field

# Base Directory Paths
PROJECT_ROOT = Path(__file__).resolve().parent
REPO_ROOT = PROJECT_ROOT.parent

class SystemConfig(BaseModel):
    # Runtime Server
    host: str = Field(default_factory=lambda: os.getenv("COLONPATH_HOST", "0.0.0.0"))
    port: int = Field(default_factory=lambda: int(os.getenv("COLONPATH_PORT", "8000")))
    max_upload_size_mb: int = Field(default_factory=lambda: int(os.getenv("COLONPATH_MAX_UPLOAD_MB", "50")))
    device: str = Field(default_factory=lambda: os.getenv("COLONPATH_DEVICE", "cpu"))
    log_level: str = Field(default_factory=lambda: os.getenv("COLONPATH_LOG_LEVEL", "INFO"))

    # Storage Paths
    output_dir: Path = Field(default_factory=lambda: Path(os.getenv("COLONPATH_OUTPUT_DIR", str(PROJECT_ROOT / "outputs"))))
    cases_dir: Path = Field(default_factory=lambda: Path(os.getenv("COLONPATH_CASES_DIR", str(PROJECT_ROOT / "outputs" / "cases"))))
    uploads_dir: Path = Field(default_factory=lambda: Path(os.getenv("COLONPATH_UPLOADS_DIR", str(PROJECT_ROOT / "outputs" / "uploads"))))
    database_path: Path = Field(default_factory=lambda: Path(os.getenv("COLONPATH_DB_PATH", str(PROJECT_ROOT / "outputs" / "colonpath_cases.db"))))

    # Model Weights Paths
    unet_weights: Path = Field(default_factory=lambda: REPO_ROOT / "cv" / "outputs" / "unet" / "best_model.pth")
    hovernet_weights: Path = Field(default_factory=lambda: REPO_ROOT / "cv" / "hovernet_reference" / "checkpoints" / "hovernet_original_consep_type_tf2pytorch")
    fusion_weights: Path = Field(default_factory=lambda: PROJECT_ROOT / "outputs" / "models" / "best_classifier.pth")
    foundation_model_id: str = Field(default_factory=lambda: os.getenv("FOUNDATION_MODEL_ID", "owkin/phikon-v2"))
    medgemma_model_id: str = Field(default_factory=lambda: os.getenv("MEDGEMMA_MODEL_ID", "google/medgemma-1.5-4b-it"))

    # Allowed Upload Extensions
    allowed_extensions: tuple = (".png", ".jpg", ".jpeg", ".bmp", ".tif", ".tiff")
    allowed_visualizations: tuple = ("original", "glands", "nuclei", "regions", "uncertainty", "top_regions", "pseudo_3d")

config = SystemConfig()

