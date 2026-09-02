"""
Targeted Phase 6 Unit Tests: Explainability, Evidence Traceability, Guardrails & Safe Language.
Fast, mocked tests that do NOT invoke heavy deep learning models.
"""

import pytest
from evidence.explainer import EvidenceGroundedExplainer
from agent.evidence_validator import EvidenceValidator
from evidence.evidence_builder import EvidenceBuilder
from fusion.feature_schema import MorphologyFeatureVector
from uncertainty.uncertainty_estimator import UncertaintyResult
from agreement.agreement_engine import AgreementResult
from regions.region_analyzer import RegionItem


@pytest.fixture
def sample_case_result():
    morph = MorphologyFeatureVector(
        case_id="CASE_P6_TEST",
        nuclei_total=117,
        nuclei_type_1=92,
        nuclei_type_2=15,
        nuclei_type_3=8,
        nuclei_type_4=2,
        nuclei_mean_area_px2=124.5,
        nuclei_mean_perimeter_px=38.2,
        nuclei_mean_eccentricity=0.45,
        nuclei_mean_circularity=0.82,
        glands_total=7,
        glands_mean_area_px2=1450.0,
        glands_mean_perimeter_px=210.0,
        glands_mean_width_px=45.0,
        glands_mean_height_px=42.0,
        glands_mean_aspect_ratio=1.07,
        glands_mean_circularity=0.489,
    )
    unc = UncertaintyResult(
        uncertainty_score=0.15,
        uncertainty_level="LOW",
        entropy=0.35,
        normalized_entropy=0.11,
        raw_confidence=0.95,
        calibrated_confidence=0.92,
        review_required=False,
        abstention_message="Sharp calibrated distribution.",
        ood_score=-12.5,
        ood_status="IN_DISTRIBUTION",
        is_ood=False,
    )
    agr = AgreementResult(
        level="HIGH",
        score=1.0,
        concordant_sources=["Fusion", "VisualEmbedding", "Cytology", "GlandularArchitecture"],
        discordant_sources=[],
        summary="High consensus across all channels.",
        nuclear_interpretation="Epithelial predominance with mild pleomorphism.",
        gland_interpretation="Preserved round glandular profiles.",
        morphology_interpretation="Preserved glandular and cellular architecture.",
        review_recommended=False,
    )
    reg = [
        RegionItem(
            region_id="R_01",
            index=0,
            x=0,
            y=0,
            width=128,
            height=128,
            prediction="NORM",
            confidence=0.95,
            tumor_probability=0.02,
            uncertainty_score=0.1,
            uncertainty_level="LOW",
            priority_score=0.15,
            priority_level="LOW",
            priority_label="Low Priority Review",
            nuclei_count=58,
            glands_count=4,
            agreement_level="HIGH",
            rationale="Normal glandular profiles.",
        )
    ]
    return EvidenceBuilder.build_case_result(
        case_id="CASE_P6_TEST",
        image_quality={"passed": True, "blur_status": "ACCEPTABLE"},
        digepath_meta={"model_name": "Phikon-v2", "architecture": "ViT-L/16"},
        prediction_result={"prediction": "NORM", "confidence": 0.95, "tumor_probability": 0.02, "binary_prediction": "NON-TUM"},
        uncertainty=unc,
        model_agreement=agr,
        morphology=morph,
        priority_regions=reg,
    )


def test_evidence_trace_presence(sample_case_result):
    """Verify evidence_trace exists and records exact quantitative metrics."""
    trace = sample_case_result.get("evidence_trace")
    assert trace is not None
    assert trace["prediction_class"] == "NORM"
    assert trace["nuclear_total_count"] == 117
    assert trace["gland_total_count"] == 7


def test_claim_generation_and_grounding(sample_case_result):
    """Verify atomic claims generated from case_result map to existing evidence fields."""
    claims = EvidenceGroundedExplainer.generate_claims(sample_case_result)
    assert len(claims) >= 5
    sources = [c["evidence_source"] for c in claims]
    assert "prediction.class" in sources
    assert "nuclear_evidence.total_count" in sources
    assert "gland_evidence.total_count" in sources
    assert "uncertainty.level" in sources


def test_safe_language_explanation(sample_case_result):
    """Verify explanation uses decision-support phrasing and avoids definitive diagnostic claims."""
    explanation = EvidenceGroundedExplainer.generate_explanation(sample_case_result)
    assert "suggests" in explanation or "AI-assisted" in explanation
    assert "confirmed cancer" not in explanation
    assert "definitive diagnosis" not in explanation
    val_res = EvidenceValidator.validate(explanation, sample_case_result)
    assert val_res.is_valid is True


def test_validator_rejects_prohibited_overclaims(sample_case_result):
    """Verify EvidenceValidator rejects prohibited claims."""
    bad_claims = [
        "Automated system confirmed cancer for this patient.",
        "This is a definitive diagnosis of malignancy.",
        "We are 100% accurate that the biopsy confirmed adenocarcinoma.",
        "Autonomous diagnosis proves cancer.",
    ]
    for bad_claim in bad_claims:
        res = EvidenceValidator.validate(bad_claim, sample_case_result)
        assert res.is_valid is False
        assert len(res.errors) >= 1


def test_validator_rejects_hallucinated_class(sample_case_result):
    """Verify EvidenceValidator catches class mismatch hallucinations."""
    hallucinated_text = "The model identified TUM and adenocarcinoma in this region."
    res = EvidenceValidator.validate(hallucinated_text, sample_case_result)
    assert res.is_valid is False
    assert any("Class hallucination" in e for e in res.errors)


def test_validator_rejects_hallucinated_cell_counts(sample_case_result):
    """Verify EvidenceValidator catches fabricated numerical counts."""
    fake_counts_text = "Analysis identified 9999 nuclei across the tissue."
    res = EvidenceValidator.validate(fake_counts_text, sample_case_result)
    assert res.is_valid is False
    assert any("Nuclear count discrepancy" in e for e in res.errors)
