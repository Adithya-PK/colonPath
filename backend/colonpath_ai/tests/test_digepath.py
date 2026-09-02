"""
Unit tests for authentic Phikon-v2 feature extractor and embedding cache.
"""

from pathlib import Path
from PIL import Image
import numpy as np
import pytest
from foundation.phikon.model_loader import FoundationModelLoader
from foundation.phikon.inference import PhikonV2FeatureExtractor
from foundation.phikon.embedding_cache import EmbeddingCache

TEST_IMAGE_PATH = Path(__file__).resolve().parents[1] / "outputs" / "hovernet_test" / "input" / "00000.png"


def test_preprocess_image():
    if not TEST_IMAGE_PATH.exists():
        pytest.skip("Test image not found")
    loader = FoundationModelLoader.get_instance()
    img = Image.open(TEST_IMAGE_PATH).convert("RGB")
    inputs = loader.processor(images=img, return_tensors="pt")
    assert "pixel_values" in inputs
    assert inputs["pixel_values"].shape == (1, 3, 224, 224)


def test_phikon_feature_extraction():
    if not TEST_IMAGE_PATH.exists():
        pytest.skip("Test image not found")
    extractor = PhikonV2FeatureExtractor()
    emb = extractor.extract(TEST_IMAGE_PATH)
    assert isinstance(emb, np.ndarray)
    assert emb.shape == (1024,)
    assert np.isclose(np.linalg.norm(emb), 1.0, atol=1e-3)


def test_embedding_cache():
    cache = EmbeddingCache(max_memory_entries=10)
    fake_emb = np.random.randn(1024).astype(np.float32)
    cache.put("test_key_001", fake_emb)
    retrieved = cache.get("test_key_001")
    assert retrieved is not None
    assert np.allclose(fake_emb, retrieved)
