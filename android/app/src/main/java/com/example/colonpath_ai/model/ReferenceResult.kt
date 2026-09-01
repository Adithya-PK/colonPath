package com.example.colonpath_ai.model

data class ComparisonMetric(
    val name: String,
    val referenceValue: String,
    val patientValue: String
)

data class ReferenceResult(
    val referenceId: String,
    val similarityScore: Double,
    val category: String,
    val relevantMetrics: List<String> = emptyList()
)

data class ReferenceComparison(
    val references: List<ReferenceResult>,
    val metrics: List<ComparisonMetric>
)
