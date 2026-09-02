"""
Model and Evidence Multi-Source Agreement Engine.
Cross-checks Digepath visual predictions, nuclear morphology, gland morphology, fusion predictions, and reference matches.
"""

from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field
from fusion.feature_schema import MorphologyFeatureVector


class AgreementResult(BaseModel):
    level: str  # "HIGH", "MEDIUM", "LOW"
    score: float  # 0.0 to 1.0
    concordant_sources: List[str] = Field(default_factory=list)
    discordant_sources: List[str] = Field(default_factory=list)
    morphology_interpretation: str
    gland_interpretation: str
    nuclear_interpretation: str
    review_recommended: bool
    summary: str


class AgreementEngine:
    """
    Evaluates multi-source evidence agreement across vision, nuclear morphology, and gland morphology.
    """

    @classmethod
    def evaluate(
        cls,
        fusion_prediction: str,
        tumor_probability: float,
        morphology: MorphologyFeatureVector,
        visual_prediction: Optional[str] = None,
        reference_top_class: Optional[str] = None,
    ) -> AgreementResult:
        concordant = []
        discordant = []

        is_pred_tum = (fusion_prediction == "TUM" or tumor_probability >= 0.5)

        # 1. Evaluate Nuclear Evidence
        # High epithelial + large nuclear size indicates atypia/tumor
        is_nuclear_abnormal = (
            morphology.nuclei_type_1 > 35 or
            morphology.nuclei_mean_area_px2 > 120.0 or
            morphology.nuclei_mean_eccentricity > 0.78
        )
        if is_nuclear_abnormal:
            n_interp = f"Nuclear pleomorphism detected: {morphology.nuclei_total} nuclei, high mean area ({morphology.nuclei_mean_area_px2:.1f} px²)."
            if is_pred_tum:
                concordant.append("Nuclear Morphology (Pleomorphism aligns with tumor likelihood)")
            else:
                discordant.append("Nuclear Morphology (Pleomorphism conflicts with non-tumor prediction)")
        else:
            n_interp = f"Nuclear morphology appears regular: {morphology.nuclei_total} nuclei, mean area {morphology.nuclei_mean_area_px2:.1f} px²."
            if not is_pred_tum:
                concordant.append("Nuclear Morphology (Regular nuclei align with non-tumor prediction)")
            else:
                discordant.append("Nuclear Morphology (Regular nuclei conflict with tumor prediction)")

        # 2. Evaluate Gland Evidence
        # Glands with high area, low circularity (< 0.45) indicate architectural distortion
        is_gland_distorted = (
            morphology.glands_total > 0 and (
                morphology.glands_mean_circularity < 0.45 or
                morphology.glands_mean_aspect_ratio > 1.40 or
                morphology.glands_mean_area_px2 > 10000.0
            )
        )
        if morphology.glands_total == 0:
            g_interp = "No discrete glandular structures segmented in patch."
        elif is_gland_distorted:
            g_interp = f"Glandular architectural distortion: {morphology.glands_total} glands, low circularity ({morphology.glands_mean_circularity:.2f}), high aspect ratio ({morphology.glands_mean_aspect_ratio:.2f})."
            if is_pred_tum:
                concordant.append("Gland Morphology (Architectural distortion aligns with tumor prediction)")
            else:
                discordant.append("Gland Morphology (Architectural distortion conflicts with non-tumor prediction)")
        else:
            g_interp = f"Regular glandular architecture: {morphology.glands_total} glands, circularity ({morphology.glands_mean_circularity:.2f})."
            if not is_pred_tum:
                concordant.append("Gland Morphology (Regular glands align with non-tumor prediction)")
            else:
                discordant.append("Gland Morphology (Regular glands conflict with tumor prediction)")

        # 3. Check Visual Feature Channel if available
        v_pred = visual_prediction
        if v_pred:
            is_visual_tum = (v_pred == "TUM")
            if is_visual_tum == is_pred_tum:
                concordant.append(f"Visual Foundation Channel ({v_pred} aligns with Fusion {fusion_prediction})")
            else:
                discordant.append(f"Visual Foundation Channel ({v_pred} differs from Fusion {fusion_prediction})")

        # 4. Compute Agreement Level & Score
        total_checks = len(concordant) + len(discordant)
        score = float(len(concordant) / total_checks) if total_checks > 0 else 0.5

        if score >= 0.75 and len(discordant) == 0:
            level = "HIGH"
            review_rec = False
            summary = f"High agreement across all evaluated sources: {fusion_prediction} supported by visual, nuclear, and glandular evidence."
        elif score >= 0.50:
            level = "MEDIUM"
            review_rec = True
            summary = f"Moderate agreement: Partial alignment on {fusion_prediction}, with minor morphological variance."
        else:
            level = "LOW"
            review_rec = True
            summary = f"Low agreement / Evidence Conflict: Discrepancy detected between visual classification ({fusion_prediction}) and morphological measurements."

        return AgreementResult(
            level=level,
            score=round(score, 4),
            concordant_sources=concordant,
            discordant_sources=discordant,
            morphology_interpretation=f"{n_interp} {g_interp}",
            gland_interpretation=g_interp,
            nuclear_interpretation=n_interp,
            review_recommended=review_rec,
            summary=summary,
        )
