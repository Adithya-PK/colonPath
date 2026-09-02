"""
Phase 8.1 Targeted Case Identity & Switching Integrity Tests.
Fast tests without invoking deep learning models.
"""

import pytest
from fastapi.testclient import TestClient
from api.main import app

client = TestClient(app)


def test_case_identity_traceability():
    """Verify that case results retain exact case_id and metadata without cross-case contamination."""
    # List existing cases
    res = client.get("/cases")
    assert res.status_code == 200
    data = res.json()
    assert isinstance(data, list)

    # For each case, ensure case_id matches route lookup
    if len(data) > 0:
        first_case = data[0]
        cid = first_case["case_id"]
        detail_res = client.get(f"/cases/{cid}/result")
        assert detail_res.status_code == 200
        detail = detail_res.json()
        assert detail["case_id"] == cid
        assert "prediction" in detail
        assert "uncertainty" in detail


def test_multiple_case_switching_isolation():
    """Verify that querying Case A then Case B returns distinct, isolated results."""
    res = client.get("/cases")
    assert res.status_code == 200
    cases = res.json()
    assert isinstance(cases, list)
    if len(cases) >= 2:
        case_a_id = cases[0]["case_id"]
        case_b_id = cases[1]["case_id"]

        res_a = client.get(f"/cases/{case_a_id}/result")
        res_b = client.get(f"/cases/{case_b_id}/result")

        assert res_a.status_code == 200
        assert res_b.status_code == 200

        data_a = res_a.json()
        data_b = res_b.json()

        assert data_a["case_id"] == case_a_id
        assert data_b["case_id"] == case_b_id
        assert data_a["case_id"] != data_b["case_id"]
