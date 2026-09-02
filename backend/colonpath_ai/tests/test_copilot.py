"""
Comprehensive Unit Tests for MedGemma Pathologist Copilot Q&A.
Verifies that the Copilot accurately answers inquiries across all clinical domains.
"""

from pathlib import Path
from fastapi.testclient import TestClient
from api.main import app

client = TestClient(app)
CASE_ID = "CASE_DEMO_00000"
TEST_IMAGE = Path(__file__).resolve().parents[1] / "outputs" / "hovernet_test" / "input" / "00000.png"
if not TEST_IMAGE.exists():
    TEST_IMAGE = Path(__file__).resolve().parents[3] / "source_material" / "sample_imgs" / "00000.png"


def test_copilot_all_pathologist_questions():
    # Pre-populate case if not already present
    if TEST_IMAGE.exists():
        with open(TEST_IMAGE, "rb") as f:
            client.post("/analyze", files={"image": ("00000.png", f, "image/png")}, data={"case_id": CASE_ID})
    test_questions = [
        ("Why was region R_03 prioritized?", "Priority Score"),
        ("What is the AI prediction and tumor probability?", "calibrated confidence"),
        ("What nuclear abnormalities and cell types were detected?", "Nuclear Cytopathology"),
        ("What gland features were segmented by U-Net?", "Glandular Histomorphometry"),
        ("Why is the model uncertain and what is the entropy score?", "Model Reliability"),
        ("What is the model agreement and are there conflicts?", "Multi-Source Consensus"),
        ("What is the tissue grading and dysplasia criteria?", "Histological Criteria"),
        ("Which region should I review next?", "Next Region Recommendation"),
        ("What is the image quality and blur variance?", "Image Quality Assessment"),
        ("What clinical recommendations and limitations apply?", "Clinical Recommendations"),
        ("Give me an executive summary of this case.", "Case Summary"),
    ]

    for q_text, expected_keyword in test_questions:
        res = client.post(
            "/copilot/ask",
            json={
                "case_id": CASE_ID,
                "question": q_text,
            },
        )
        assert res.status_code == 200, f"Failed on question: {q_text}"
        data = res.json()
        assert data["case_id"] == CASE_ID
        assert data["validated"] is True, f"Anti-hallucination failed on: {q_text}"
        assert expected_keyword.lower() in data["answer"].lower(), f"Missing keyword '{expected_keyword}' in answer: {data['answer']}"
