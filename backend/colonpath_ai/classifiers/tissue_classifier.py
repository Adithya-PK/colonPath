"""
High-Level Tissue Classifier Wrapper for Inference and Deployment.
"""

import json
from pathlib import Path
from typing import Optional, Union, Dict, Any
import numpy as np
import torch
import torch.nn.functional as F

from fusion.fusion_model import MultimodalFusionNet, TISSUE_CLASSES, NUM_CLASSES
from fusion.normalization import FeatureNormalizer
from fusion.feature_schema import MorphologyFeatureVector

DEFAULT_MODEL_PATH = Path(__file__).resolve().parents[1] / "outputs" / "models" / "best_classifier.pth"
DEFAULT_NORM_PATH = Path(__file__).resolve().parents[1] / "outputs" / "models" / "normalization_params.json"


class TissueClassifier:
    """
    Inference interface for multimodal tissue classification.
    """

    def __init__(
        self,
        model_path: Optional[Union[str, Path]] = None,
        norm_path: Optional[Union[str, Path]] = None,
        device: Optional[str] = None,
    ):
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self.model_path = Path(model_path or DEFAULT_MODEL_PATH)
        self.norm_path = Path(norm_path or DEFAULT_NORM_PATH)

        self.normalizer = FeatureNormalizer.load(self.norm_path)
        self.model = MultimodalFusionNet()

        if not self.model_path.exists():
            raise FileNotFoundError(
                f"Trained MultimodalFusionNet classifier checkpoint not found at '{self.model_path}'. "
                f"Execution strictly forbids operating with unweighted/random classification models."
            )

        checkpoint = torch.load(self.model_path, map_location=self.device, weights_only=False)
        if "model_state_dict" in checkpoint:
            self.model.load_state_dict(checkpoint["model_state_dict"])
        else:
            self.model.load_state_dict(checkpoint)

        self.model.to(self.device)
        self.model.eval()

    def predict(
        self, visual_embedding: np.ndarray, morphology: Union[np.ndarray, MorphologyFeatureVector]
    ) -> Dict[str, Any]:
        """
        Runs multimodal inference.

        Args:
            visual_embedding: 1024-d visual feature vector.
            morphology: 16-d raw morphology feature vector or MorphologyFeatureVector.

        Returns:
            Dictionary with prediction, confidence, tumor probability, and class probabilities.
        """
        if isinstance(morphology, MorphologyFeatureVector):
            raw_morph = morphology.to_numpy()
        else:
            raw_morph = np.asarray(morphology, dtype=np.float32)

        # Normalize morphology
        norm_morph = self.normalizer.transform(raw_morph.reshape(1, -1))

        v_tensor = torch.from_numpy(visual_embedding).float().to(self.device)
        m_tensor = torch.from_numpy(norm_morph).float().to(self.device)

        with torch.no_grad():
            mc_logits, bin_logits, latent = self.model(v_tensor, m_tensor)
            
            # Apply temperature scaling to calibrate confidence (T=2.2)
            temperature = 2.2
            cal_mc_logits = mc_logits / temperature
            cal_bin_logits = bin_logits / temperature
            
            mc_probs = F.softmax(cal_mc_logits, dim=-1).squeeze(0).cpu().numpy()
            bin_probs = F.softmax(cal_bin_logits, dim=-1).squeeze(0).cpu().numpy()
            latent_vec = latent.squeeze(0).cpu().numpy()

        pred_idx = int(np.argmax(mc_probs))
        pred_class = TISSUE_CLASSES[pred_idx]
        confidence = float(mc_probs[pred_idx])

        # Compute calibrated prediction entropy: H = - sum(p * log(p))
        eps = 1e-10
        entropy = float(-np.sum(mc_probs * np.log(mc_probs + eps)))
        max_entropy = np.log(NUM_CLASSES)
        normalized_entropy = float(entropy / max_entropy)

        # Derived calibrated tumor likelihood from multiclass distribution
        # TUM is index 8 in TISSUE_CLASSES
        tum_idx = TISSUE_CLASSES.index("TUM") if "TUM" in TISSUE_CLASSES else 8
        multiclass_tum_prob = float(mc_probs[tum_idx])
        combined_tum_prob = float(0.6 * multiclass_tum_prob + 0.4 * bin_probs[1])

        return {
            "prediction": pred_class,
            "prediction_index": pred_idx,
            "confidence": confidence,
            "entropy": entropy,
            "normalized_entropy": normalized_entropy,
            "multiclass_probabilities": {
                cls_name: float(p) for cls_name, p in zip(TISSUE_CLASSES, mc_probs)
            },
            "tumor_probability": combined_tum_prob,
            "non_tumor_probability": float(1.0 - combined_tum_prob),
            "binary_prediction": "TUM" if combined_tum_prob >= 0.5 else "NON-TUM",
            "latent_vector": latent_vec,
            "logits": mc_logits.squeeze(0).cpu().numpy(),
        }


def get_tissue_classifier(
    model_path: Optional[Union[str, Path]] = None,
    norm_path: Optional[Union[str, Path]] = None,
    device: Optional[str] = None,
) -> TissueClassifier:
    return TissueClassifier(model_path=model_path, norm_path=norm_path, device=device)
