"""
Targeted Phase 7 Unit Tests: Case Lifecycle, End-to-End API Contracts, Copilot Isolation, & Security.
Fast, mocked tests that do NOT invoke heavy deep learning models.
"""

import pytest
from fastapi.testclient import TestClient
from api.main import app
from orchestrator.pipeline import CaseStage
from agent.medgemma_vlm import MedGemmaVLM
from api.services.case_service import CaseService, _ACTIVE_PROCESSING_CASES

client = TestClient(app)


def test_health_check_contract():
    """Verify health endpoint returns valid status, models metadata and version 2.0.0."""
    r = client.get("/health")
    assert r.status_code == 200
    data = r.json()
    assert data["status"].lower() == "healthy"
    assert data["version"] == "2.0.0"
    assert isinstance(data["models_ready"], bool)


def test_copilot_case_session_isolation():
    """Verify that Copilot queries strictly scope to the requested case."""
    vlm = MedGemmaVLM()
    case_a = {
        "case_id": "CASE_SPEC_A",
        "prediction": {"class": "NORM", "calibrated_confidence": 0.98, "tumor_probability": 0.01},
        "nuclear_evidence": {"total_count": 117, "mean_area_px2": 124.5, "mean_circularity": 0.82},
        "gland_evidence": {"total_count": 7, "mean_circularity": 0.489, "mean_aspect_ratio": 1.07},
        "uncertainty": {"level": "LOW", "score": 0.12},
        "model_agreement": {"level": "HIGH", "score": 1.0},
        "reference_comparison": {"top_category": "normal", "top_similarity_percent": 91.4},
        "priority_regions": [],
        "image_quality": {"passed": True},
    }
    case_b = {
        "case_id": "CASE_SPEC_B",
        "prediction": {"class": "TUM", "calibrated_confidence": 0.95, "tumor_probability": 0.96},
        "nuclear_evidence": {"total_count": 450, "mean_area_px2": 210.0, "mean_circularity": 0.65},
        "gland_evidence": {"total_count": 22, "mean_circularity": 0.28, "mean_aspect_ratio": 2.15},
        "uncertainty": {"level": "LOW", "score": 0.18},
        "model_agreement": {"level": "HIGH", "score": 0.95},
        "reference_comparison": {"top_category": "tumor", "top_similarity_percent": 88.7},
        "priority_regions": [],
        "image_quality": {"passed": True},
    }

    res_a = vlm.answer_copilot_question(question="What is the cell count and prediction?", case_result=case_a)
    assert res_a["case_id"] == "CASE_SPEC_A"
    assert "117" in res_a["answer"]
    assert "NORM" in res_a["answer"]
    assert "TUM" not in res_a["answer"]
    assert res_a["validated"] is True

    res_b = vlm.answer_copilot_question(question="What is the cell count and prediction?", case_result=case_b)
    assert res_b["case_id"] == "CASE_SPEC_B"
    assert "450" in res_b["answer"]
    assert "TUM" in res_b["answer"]
    assert res_b["validated"] is True


def test_copilot_unsupported_molecular_domain():
    """Verify that Copilot explicitly declines unsupported molecular/IHC inquiries."""
    vlm = MedGemmaVLM()
    case_mock = {
        "case_id": "CASE_MOLECULAR_TEST",
        "prediction": {"class": "NORM", "calibrated_confidence": 0.98, "tumor_probability": 0.01},
        "nuclear_evidence": {"total_count": 100, "mean_area_px2": 120.0, "mean_circularity": 0.8},
        "gland_evidence": {"total_count": 5, "mean_circularity": 0.5, "mean_aspect_ratio": 1.0},
        "uncertainty": {"level": "LOW", "score": 0.1},
        "model_agreement": {"level": "HIGH", "score": 1.0},
        "reference_comparison": {"top_category": "normal", "top_similarity_percent": 90.0},
        "priority_regions": [],
        "image_quality": {"passed": True},
    }
    res = vlm.answer_copilot_question(question="What is the MSI and KRAS status?", case_result=case_mock)
    assert "cannot establish mismatch repair" in res["answer"].lower() or "limitation" in res["answer"].lower()
    assert res["validated"] is True


def test_visualization_whitelist_enforcement():
    """Verify that unauthorized visualization types are rejected with 400 Bad Request."""
    r = client.get("/cases/CASE_DEMO_00000/visualization/malicious_shell_exec")
    assert r.status_code == 400
    data = r.json()
    assert data["error_code"] == "VALIDATION_ERROR"
    assert "Invalid visualization type" in data["message"]


def test_case_lifecycle_state_machine():
    """Verify complete CaseStage lifecycle enum ordering and presence."""
    stages = [s.value for s in CaseStage]
    assert stages == [
        "RECEIVED",
        "VALIDATING",
        "PREPROCESSING",
        "CV_PROCESSING",
        "FEATURE_EXTRACTION",
        "FUSION",
        "UNCERTAINTY",
        "AGREEMENT",
        "GENERATING_VISUALIZATIONS",
        "COMPLETED",
        "FAILED",
    ]
