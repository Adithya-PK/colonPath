package com.example.colonpath_ai.model

enum class CaseStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, PENDING_REVIEW
}

data class PatientInfo(
    val patientId: String,
    val patientName: String,
    val notes: String = ""
)

data class Case(
    val caseId: String,
    val patient: PatientInfo,
    val tissue: String,
    val sampleType: String,
    val stain: String,
    val analysisDate: String,
    val status: CaseStatus,
    val notes: String = ""
)
