"""
Targeted Phase 5 Unit Tests: Error Handling, State Machine, Timing, and Idempotency.
Fast, mocked tests that do NOT invoke heavy deep learning inference.
"""

import pytest
from fastapi.testclient import TestClient
from api.main import app
from api.exceptions import ValidationError, CaseNotFoundError, CaseAlreadyProcessingError, PipelineExecutionError
from api.routes.cases import validate_case_id
from orchestrator.pipeline import CaseStage
from api.services.case_service import _ACTIVE_PROCESSING_CASES

client = TestClient(app)


def test_structured_validation_error():
    """Verify that validation errors return structured JSON with error_code and stage."""
    r = client.post("/analyze")
    assert r.status_code == 422
    data = r.json()
    assert "error_code" in data
    assert data["error_code"] == "REQUEST_VALIDATION_ERROR"
    assert data["retryable"] is False


def test_structured_case_not_found_error():
    """Verify that missing cases return structured 404 JSON with CASE_NOT_FOUND."""
    r = client.get("/cases/NON_EXISTENT_CASE_12345/result")
    assert r.status_code == 404
    data = r.json()
    assert data["error_code"] == "CASE_NOT_FOUND"
    assert "NON_EXISTENT_CASE_12345" in data["case_id"]
    assert data["retryable"] is False


def test_path_traversal_structured_rejection():
    """Verify that path traversal in case_id is rejected by validate_case_id with ValidationError."""
    with pytest.raises(ValidationError) as exc_info:
        validate_case_id("../../etc/passwd")
    assert exc_info.value.status_code == 400
    assert exc_info.value.error_code == "VALIDATION_ERROR"

    # Also test via route
    r = client.get("/cases/..%2F..%2Fescape/result")
    assert r.status_code in [400, 404]


def test_idempotency_concurrent_rejection():
    """Verify that a case in _ACTIVE_PROCESSING_CASES is rejected with 409 Conflict."""
    test_cid = "CONCURRENT_TEST_CASE"
    _ACTIVE_PROCESSING_CASES.add(test_cid)
    try:
        from api.services.case_service import CaseService
        from pathlib import Path
        service = CaseService()
        with pytest.raises(CaseAlreadyProcessingError) as exc_info:
            service.analyze_image(image_path=Path("dummy.png"), case_id=test_cid)
        assert exc_info.value.status_code == 409
        assert exc_info.value.error_code == "CASE_ALREADY_PROCESSING"
        assert exc_info.value.retryable is True
    finally:
        _ACTIVE_PROCESSING_CASES.discard(test_cid)


def test_pipeline_stage_enum_completeness():
    """Verify all required lifecycle states exist in CaseStage enum."""
    expected_states = {
        "RECEIVED", "VALIDATING", "PREPROCESSING", "CV_PROCESSING",
        "FEATURE_EXTRACTION", "FUSION", "UNCERTAINTY",
        "AGREEMENT", "GENERATING_VISUALIZATIONS",
        "COMPLETED", "FAILED"
    }
    actual_states = {s.value for s in CaseStage}
    assert expected_states.issubset(actual_states)
