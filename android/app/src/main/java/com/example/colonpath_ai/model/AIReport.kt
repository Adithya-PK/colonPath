package com.example.colonpath_ai.model

data class Limitations(
    val items: List<String>
)

data class PathologistReview(
    val required: Boolean = true,
    val message: String
)

data class AIReport(
    val summary: String,
    val computationalFindings: String,
    val interpretation: String,
    val supportingEvidence: List<String>,
    val limitations: Limitations,
    val uncertainty: String,
    val pathologistReview: PathologistReview
)
