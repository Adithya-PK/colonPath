"""
Canonical Exception Definitions for COLONPATH-AI.
"""

from typing import Optional

class ColonPathException(Exception):
    def __init__(
        self,
        error_code: str,
        message: str,
        status_code: int = 500,
        case_id: Optional[str] = None,
        stage: Optional[str] = None,
        retryable: bool = False,
    ):
        super().__init__(message)
        self.error_code = error_code
        self.message = message
        self.status_code = status_code
        self.case_id = case_id
        self.stage = stage
        self.retryable = retryable

class ValidationError(ColonPathException):
    def __init__(self, message: str, case_id: Optional[str] = None, stage: str = "VALIDATING"):
        super().__init__(
            error_code="VALIDATION_ERROR",
            message=message,
            status_code=400,
            case_id=case_id,
            stage=stage,
            retryable=False,
        )

class CaseNotFoundError(ColonPathException):
    def __init__(self, case_id: str):
        super().__init__(
            error_code="CASE_NOT_FOUND",
            message=f"Case '{case_id}' was not found in the database or filesystem.",
            status_code=404,
            case_id=case_id,
            stage="RETRIEVAL",
            retryable=False,
        )

class CaseAlreadyProcessingError(ColonPathException):
    def __init__(self, case_id: str):
        super().__init__(
            error_code="CASE_ALREADY_PROCESSING",
            message=f"Case '{case_id}' is currently being analyzed. Concurrent duplicate requests are rejected.",
            status_code=409,
            case_id=case_id,
            stage="PROCESSING",
            retryable=True,
        )

class PipelineExecutionError(ColonPathException):
    def __init__(self, message: str, case_id: Optional[str] = None, stage: Optional[str] = None, retryable: bool = True):
        super().__init__(
            error_code="PIPELINE_EXECUTION_ERROR",
            message=message,
            status_code=500,
            case_id=case_id,
            stage=stage,
            retryable=retryable,
        )

class FoundationModelUnavailableError(ColonPathException):
    def __init__(self, message: str, case_id: Optional[str] = None):
        super().__init__(
            error_code="FOUNDATION_MODEL_UNAVAILABLE",
            message=message,
            status_code=503,
            case_id=case_id,
            stage="FEATURE_EXTRACTION",
            retryable=False,
        )
