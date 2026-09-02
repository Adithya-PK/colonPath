"""
FastAPI Main Application Entry Point for COLONPATH-AI.
"""

import logging
from pathlib import Path
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse, FileResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.exceptions import RequestValidationError

from api.exceptions import ColonPathException
from api.routes import health, analysis, cases, regions, review, copilot

logger = logging.getLogger("colonpath_api")

app = FastAPI(
    title="COLONPATH-AI Decision Support API",
    description="Multimodal GI Foundation Model & Decision Support System for Colorectal Histopathology",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# Exception handlers for structured, safe API errors
@app.exception_handler(ColonPathException)
async def colonpath_exception_handler(request: Request, exc: ColonPathException):
    logger.error(f"[COLONPATH_EXCEPTION] Code: {exc.error_code} | Stage: {exc.stage} | Case: {exc.case_id} | Msg: {exc.message}")
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error_code": exc.error_code,
            "message": exc.message,
            "case_id": exc.case_id,
            "stage": exc.stage,
            "retryable": exc.retryable,
        },
    )

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    logger.warning(f"[HTTP_EXCEPTION] Status: {exc.status_code} | Detail: {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error_code": "HTTP_ERROR" if exc.status_code != 404 else "NOT_FOUND",
            "message": str(exc.detail),
            "case_id": None,
            "stage": None,
            "retryable": exc.status_code >= 500,
        },
    )

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.warning(f"[VALIDATION_EXCEPTION] {exc.errors()}")
    return JSONResponse(
        status_code=422,
        content={
            "error_code": "REQUEST_VALIDATION_ERROR",
            "message": "Invalid request parameters or payload structure.",
            "case_id": None,
            "stage": "VALIDATING",
            "retryable": False,
        },
    )

@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    logger.exception(f"[UNHANDLED_EXCEPTION] {str(exc)}")
    return JSONResponse(
        status_code=500,
        content={
            "error_code": "INTERNAL_SERVER_ERROR",
            "message": "An internal error occurred while processing the histopathology request.",
            "case_id": None,
            "stage": None,
            "retryable": True,
        },
    )

# CORS middleware for mobile & web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router)
app.include_router(analysis.router)
app.include_router(cases.router)
app.include_router(regions.router)
app.include_router(regions.alias_router)
app.include_router(review.router)
app.include_router(copilot.router)

# Mount outputs directory for static access if needed
outputs_dir = Path(__file__).resolve().parents[1] / "outputs"
outputs_dir.mkdir(parents=True, exist_ok=True)
app.mount("/static", StaticFiles(directory=str(outputs_dir)), name="static")

WEB_DIR = Path(__file__).resolve().parents[1] / "web"

@app.get("/")
@app.get("/viewer")
def root_dashboard():
    index_file = WEB_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return {
        "system": "COLONPATH-AI",
        "description": "Multimodal GI Foundation Decision Support API",
        "documentation": "/docs",
        "health": "/health",
    }
