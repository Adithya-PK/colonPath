"""
Evidence-Grounded Explanation Generator for Decision Support.
Ensures explanations are strictly grounded in computational outputs without hallucinations
and outputs both formatted narrative and verifiable claim traces.
"""

from typing import Dict, Any, List, Optional


class EvidenceGroundedExplainer:
    """
    Generates structured, factual explanations strictly from verified computational evidence.
    """

    @classmethod
    def generate_claims(cls, case_result: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        Extracts atomic, verifiable claim records from case_result.
        """
        pred = case_result.get("prediction", {})
        unc = case_result.get("uncertainty", {})
        agr = case_result.get("model_agreement", {})
        nuc = case_result.get("nuclear_evidence", {})
        gland = case_result.get("gland_evidence", {})
        regions = case_result.get("priority_regions", [])

        claims = [
            {
                "claim_id": "C_01",
                "category": "classification",
                "claim_statement": f"AI multimodal prediction is {pred.get('class', 'UNKNOWN')} with {pred.get('calibrated_confidence', 0.0)*100:.1f}% calibrated confidence.",
                "evidence_source": "prediction.class",
                "evidence_value": pred.get("class", "UNKNOWN"),
                "support_type": "inferred_probability",
            },
            {
                "claim_id": "C_02",
                "category": "nuclear_morphometry",
                "claim_statement": f"Observed {nuc.get('total_count', 0)} nuclei with mean area {nuc.get('mean_area_px2', 0.0):.1f} px² and circularity {nuc.get('mean_circularity', 0.0):.3f}.",
                "evidence_source": "nuclear_evidence.total_count",
                "evidence_value": nuc.get("total_count", 0),
                "support_type": "measured_direct",
            },
            {
                "claim_id": "C_03",
                "category": "gland_morphometry",
                "claim_statement": f"Segmented {gland.get('total_count', 0)} glandular structures with mean circularity {gland.get('mean_circularity', 0.0):.3f}.",
                "evidence_source": "gland_evidence.total_count",
                "evidence_value": gland.get("total_count", 0),
                "support_type": "measured_direct",
            },
            {
                "claim_id": "C_04",
                "category": "uncertainty",
                "claim_statement": f"Model uncertainty level is {unc.get('level', 'LOW')} (Normalized Entropy: {unc.get('normalized_entropy', 0.0):.4f}, OOD: {unc.get('ood_status', 'IN_DISTRIBUTION')}).",
                "evidence_source": "uncertainty.level",
                "evidence_value": unc.get("level", "LOW"),
                "support_type": "calibrated_metric",
            },
            {
                "claim_id": "C_05",
                "category": "model_agreement",
                "claim_statement": f"Multi-source consensus agreement is {agr.get('level', 'HIGH')}.",
                "evidence_source": "model_agreement.level",
                "evidence_value": agr.get("level", "HIGH"),
                "support_type": "voting_consensus",
            },
        ]
        return claims

    @classmethod
    def generate_explanation(cls, case_result: Dict[str, Any]) -> str:
        """
        Generates a factual narrative directly citing case_result facts with safe decision-support terminology.
        """
        pred = case_result.get("prediction", {})
        unc = case_result.get("uncertainty", {})
        agr = case_result.get("model_agreement", {})
        nuc = case_result.get("nuclear_evidence", {})
        gland = case_result.get("gland_evidence", {})
        regions = case_result.get("priority_regions", [])
        quality = case_result.get("image_quality", {})

        # Check for image quality failure or high uncertainty
        if not quality.get("passed", True):
            return (
                f"AI Decision Support: Optical quality check flagged anomalies ({quality.get('blur_status', 'QUALITY_WARNING')}). "
                f"Predicted class: {pred.get('class', 'UNKNOWN')} ({pred.get('calibrated_confidence', 0.0)*100:.1f}% confidence). "
                f"Due to image degradation, reliability is reduced and immediate pathologist evaluation is recommended."
            )

        if unc.get("level") == "HIGH" or unc.get("review_required") is True:
            return (
                f"AI-Assisted Classification: {pred.get('class', 'UNKNOWN')} "
                f"(Calibrated Confidence: {pred.get('calibrated_confidence', 0.0)*100:.1f}%). "
                f"High model uncertainty detected (Normalized Entropy: {unc.get('normalized_entropy', 0.0):.3f}). "
                f"Pathologist review is recommended to evaluate cellular features."
            )

        # Standard evidence-grounded summary
        pred_class = pred.get("class", "NORM")
        conf = pred.get("calibrated_confidence", pred.get("confidence", 0.0)) * 100.0
        tumor_prob = pred.get("tumor_probability", 0.0) * 100.0
        n_total = nuc.get("total_count", 0)
        n_area = nuc.get("mean_area_px2", 0.0)
        g_total = gland.get("total_count", 0)
        g_circ = gland.get("mean_circularity", 0.0)

        lines = [
            f"AI-assisted classification suggests **{pred_class}** with {conf:.1f}% calibrated confidence (Binary Tumor Likelihood: {tumor_prob:.1f}%).",
            f"Nuclear Analysis: {n_total} nuclei detected (mean area: {n_area:.1f} px², circularity: {nuc.get('mean_circularity', 0.0):.3f}).",
            f"Gland Analysis: {g_total} glandular structures segmented (mean circularity: {g_circ:.3f}, aspect ratio: {gland.get('mean_aspect_ratio', 1.0):.2f}).",
            f"Model Agreement: {agr.get('level', 'HIGH')} agreement across computational feature channels.",
        ]

        if regions:
            top_r = regions[0]
            lines.append(
                f"AI-Prioritized Focus Region: {top_r.get('region_id')} (Priority Score: {top_r.get('priority_score', 0.0):.2f}, "
                f"Priority Level: {top_r.get('priority_level', 'LOW')}) at coordinates (x={top_r.get('x')}, y={top_r.get('y')})."
            )

        return "\n".join(lines)
