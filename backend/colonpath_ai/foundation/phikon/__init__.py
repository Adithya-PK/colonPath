"""
Phikon-v2 Foundation Model Package.
Provides verified, pretrained 1024D visual feature extraction for computational histopathology.
"""

from .model_loader import FoundationModelLoader, get_foundation_model
from .inference import PhikonV2FeatureExtractor
from .embedding_cache import EmbeddingCache

__all__ = [
    "FoundationModelLoader",
    "get_foundation_model",
    "PhikonV2FeatureExtractor",
    "EmbeddingCache",
]
