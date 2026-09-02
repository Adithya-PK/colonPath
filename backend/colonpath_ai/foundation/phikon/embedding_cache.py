"""
Model-Aware Embedding Caching Module for Foundation Visual Representations.
Provides cryptographically robust provenance and cache isolation per model and image content.
"""

import hashlib
import json
import logging
from pathlib import Path
from typing import Optional, Dict, Union, Any
import numpy as np
from PIL import Image

logger = logging.getLogger(__name__)


class EmbeddingCache:
    """
    Model-Aware Two-Tier (Memory + Disk) embedding cache.
    Guarantees strict cache isolation by hashing image content SHA-256 together with
    the exact foundation model identifier, weights signature, and preprocessing version.
    """

    def __init__(
        self,
        cache_dir: Optional[Union[str, Path]] = None,
        max_memory_entries: int = 1000,
        model_id: str = "owkin/phikon-v2",
        weights_signature: str = "dinov2_vit_large_1024d_official",
        preproc_version: str = "BitImageProcessor_224_bicubic",
        extractor_version: str = "2.0.0",
    ):
        if cache_dir is None:
            # New namespace directory strictly isolating from legacy/untrusted caches
            self.cache_dir = Path(__file__).resolve().parents[2] / ".cache" / "embeddings" / "v2_phikon"
        else:
            self.cache_dir = Path(cache_dir)

        self.cache_dir.mkdir(parents=True, exist_ok=True)
        self.max_memory_entries = max_memory_entries
        self.model_id = model_id
        self.weights_signature = weights_signature
        self.preproc_version = preproc_version
        self.extractor_version = extractor_version
        self._memory_cache: Dict[str, np.ndarray] = {}

    def compute_cache_key(
        self,
        image_input: Union[str, Path, Image.Image, np.ndarray, bytes],
        custom_key: Optional[str] = None,
    ) -> str:
        """
        Computes a deterministic, model-aware SHA-256 cache key.
        Incorporates:
          1. Image content SHA-256 (or custom unique key)
          2. Foundation model identifier
          3. Checkpoint/weights signature
          4. Preprocessing version
          5. Extractor version
        """
        # 1. Extract image content hash
        if isinstance(image_input, (str, Path)):
            p = Path(image_input)
            if p.exists() and p.is_file():
                with open(p, "rb") as f:
                    img_sha = hashlib.sha256(f.read()).hexdigest()
            else:
                img_sha = hashlib.sha256(str(image_input).encode("utf-8")).hexdigest()
        elif isinstance(image_input, bytes):
            img_sha = hashlib.sha256(image_input).hexdigest()
        elif isinstance(image_input, Image.Image):
            img_sha = hashlib.sha256(image_input.tobytes()).hexdigest()
        elif isinstance(image_input, np.ndarray):
            img_sha = hashlib.sha256(image_input.tobytes()).hexdigest()
        elif custom_key:
            img_sha = hashlib.sha256(custom_key.encode("utf-8")).hexdigest()
        else:
            img_sha = hashlib.sha256(str(image_input).encode("utf-8")).hexdigest()

        # 2. Build full metadata payload for hashing
        payload = {
            "image_content_sha256": img_sha,
            "custom_key": custom_key or "",
            "foundation_model_id": self.model_id,
            "weights_signature": self.weights_signature,
            "preprocessing_version": self.preproc_version,
            "extractor_version": self.extractor_version,
        }
        serialized = json.dumps(payload, sort_keys=True)
        return hashlib.sha256(serialized.encode("utf-8")).hexdigest()

    def get(
        self,
        image_input: Union[str, Path, Image.Image, np.ndarray, bytes],
        custom_key: Optional[str] = None,
    ) -> Optional[np.ndarray]:
        """
        Retrieves embedding if and only if the exact model, weights, and image match.
        """
        key = self.compute_cache_key(image_input, custom_key)

        # 1. Memory check
        if key in self._memory_cache:
            return self._memory_cache[key]

        # 2. Disk check
        disk_file = self.cache_dir / f"{key}.npy"
        if disk_file.exists():
            try:
                emb = np.load(disk_file)
                if len(self._memory_cache) < self.max_memory_entries:
                    self._memory_cache[key] = emb
                return emb
            except Exception as e:
                logger.warning(f"Failed to read cached embedding {disk_file}: {e}")
                return None

        return None

    def put(
        self,
        image_input: Union[str, Path, Image.Image, np.ndarray, bytes],
        embedding: np.ndarray,
        custom_key: Optional[str] = None,
    ) -> None:
        """
        Stores embedding in memory and on disk under the model-aware key.
        """
        key = self.compute_cache_key(image_input, custom_key)
        emb_array = np.asarray(embedding, dtype=np.float32)

        # Memory store
        if len(self._memory_cache) >= self.max_memory_entries:
            first_key = next(iter(self._memory_cache))
            self._memory_cache.pop(first_key)
        self._memory_cache[key] = emb_array

        # Disk store
        disk_file = self.cache_dir / f"{key}.npy"
        np.save(disk_file, emb_array)

    def contains(
        self,
        image_input: Union[str, Path, Image.Image, np.ndarray, bytes],
        custom_key: Optional[str] = None,
    ) -> bool:
        key = self.compute_cache_key(image_input, custom_key)
        if key in self._memory_cache:
            return True
        return (self.cache_dir / f"{key}.npy").exists()

    def clear(self) -> None:
        self._memory_cache.clear()
        for f in self.cache_dir.glob("*.npy"):
            try:
                f.unlink()
            except Exception:
                pass
