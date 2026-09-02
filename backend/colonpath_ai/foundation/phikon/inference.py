"""
Authentic Phikon-v2 Feature Extractor Interface.
Extracts verified 1024D pathology foundation embeddings for colorectal histopathology specimens.
"""

import logging
from pathlib import Path
from typing import Union, List, Optional, Dict, Any
import numpy as np
from PIL import Image
import torch

from .model_loader import FoundationModelLoader, EMBEDDING_DIM
from .embedding_cache import EmbeddingCache

logger = logging.getLogger(__name__)


class PhikonV2FeatureExtractor:
    """
    High-level feature extractor using frozen, verified Owkin Phikon-v2 foundation model.
    Extracts 1024D CLS token representations with model-aware disk caching and L2 normalization.
    """

    def __init__(
        self,
        device: Optional[str] = None,
        use_cache: bool = True,
        cache_dir: Optional[Union[str, Path]] = None,
    ):
        self.loader = FoundationModelLoader.get_instance(device=device)
        self.model = self.loader.model
        self.processor = self.loader.processor
        self.device = self.loader.device
        self.use_cache = use_cache

        info = self.loader.info
        self.cache = EmbeddingCache(
            cache_dir=cache_dir,
            model_id=info.get("model_id", "owkin/phikon-v2"),
            weights_signature=info.get("weights_signature", "phikon_v2_official"),
            preproc_version=info.get("preprocessing_version", "BitImageProcessor_224_bicubic"),
            extractor_version=info.get("extractor_version", "2.0.0"),
        ) if use_cache else None

    @property
    def embedding_dim(self) -> int:
        return EMBEDDING_DIM

    @property
    def metadata(self) -> Dict[str, Any]:
        return self.loader.info

    def _prepare_pil_image(self, image_input: Union[str, Path, Image.Image, np.ndarray]) -> Image.Image:
        """
        Converts any supported image input to a standardized RGB PIL Image.
        """
        if isinstance(image_input, (str, Path)):
            path = Path(image_input)
            if not path.exists():
                raise FileNotFoundError(f"Image file not found: {path}")
            return Image.open(path).convert("RGB")
        elif isinstance(image_input, np.ndarray):
            if image_input.ndim == 2:
                return Image.fromarray(image_input).convert("RGB")
            elif image_input.shape[2] == 4:
                return Image.fromarray(image_input).convert("RGB")
            else:
                return Image.fromarray(image_input)
        elif isinstance(image_input, Image.Image):
            return image_input.convert("RGB")
        else:
            raise TypeError(f"Unsupported image input type: {type(image_input)}")

    def extract(
        self,
        image_input: Union[str, Path, Image.Image, np.ndarray],
        cache_key: Optional[str] = None,
    ) -> np.ndarray:
        """
        Extracts verified 1024-dimensional feature embedding for a single image patch.

        Args:
            image_input: Filepath, PIL Image, or Numpy array.
            cache_key: Optional explicit cache key identifier.

        Returns:
            np.ndarray of shape (1024,), dtype float32, L2-normalized.
        """
        # 1. Check model-aware cache
        if self.use_cache and self.cache is not None:
            cached = self.cache.get(image_input, custom_key=cache_key)
            if cached is not None:
                return cached

        # 2. Canonical Preprocessing via official AutoImageProcessor
        pil_img = self._prepare_pil_image(image_input)
        inputs = self.processor(pil_img, return_tensors="pt")
        inputs = {k: v.to(self.device) for k, v in inputs.items()}

        # 3. Forward pass in inference mode
        with torch.inference_mode():
            outputs = self.model(**inputs)
            # Official Phikon-v2 DINOv2 feature: CLS token at index 0
            if hasattr(outputs, "last_hidden_state"):
                cls_token = outputs.last_hidden_state[:, 0, :]  # [1, 1024]
            elif hasattr(outputs, "pooler_output") and outputs.pooler_output is not None:
                cls_token = outputs.pooler_output
            else:
                cls_token = outputs[0][:, 0, :]

            emb = cls_token.squeeze(0).cpu().numpy().astype(np.float32)

        # 4. Explicit L2 normalization for multimodal cosine alignment
        norm = np.linalg.norm(emb)
        if norm > 1e-8:
            emb = emb / norm

        # 5. Cache result with full provenance
        if self.use_cache and self.cache is not None:
            self.cache.put(image_input, emb, custom_key=cache_key)

        return emb

    def extract_batch(
        self,
        image_inputs: List[Union[str, Path, Image.Image, np.ndarray]],
        batch_size: int = 16,
    ) -> np.ndarray:
        """
        Extracts embeddings for a batch of images.

        Returns:
            np.ndarray of shape (N, 1024), dtype float32, L2-normalized.
        """
        if not image_inputs:
            return np.empty((0, EMBEDDING_DIM), dtype=np.float32)

        embeddings = []
        for i in range(0, len(image_inputs), batch_size):
            batch_slice = image_inputs[i : i + batch_size]
            pil_images = [self._prepare_pil_image(item) for item in batch_slice]

            inputs = self.processor(pil_images, return_tensors="pt")
            inputs = {k: v.to(self.device) for k, v in inputs.items()}

            with torch.inference_mode():
                outputs = self.model(**inputs)
                if hasattr(outputs, "last_hidden_state"):
                    cls_tokens = outputs.last_hidden_state[:, 0, :]  # [B, 1024]
                else:
                    cls_tokens = outputs[0][:, 0, :]

                batch_emb = cls_tokens.cpu().numpy().astype(np.float32)

            # L2 normalize each vector
            norms = np.linalg.norm(batch_emb, axis=1, keepdims=True)
            norms[norms < 1e-8] = 1.0
            batch_emb = batch_emb / norms

            embeddings.append(batch_emb)

        return np.vstack(embeddings)


# Compatibility alias for existing code references
DigepathFeatureExtractor = PhikonV2FeatureExtractor
