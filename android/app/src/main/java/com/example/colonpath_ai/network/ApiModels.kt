package com.example.colonpath_ai.network

/**
 * Data Transfer Objects corresponding to the canonical FastAPI CaseResult schema.
 */
data class CaseResultDto(
    val case_id: String,
    val timestamp: String,
    val status: String,
    val lifecycle_state: String? = "COMPLETED",
    val image_quality: ImageQualityDto? = null,
    val digepath: DigepathMetaDto? = null,
    val prediction: PredictionDto? = null,
    val uncertainty: UncertaintyDto? = null,
    val model_agreement: ModelAgreementDto? = null,
    val nuclear_evidence: NuclearEvidenceDto? = null,
    val gland_evidence: GlandEvidenceDto? = null,
    val reference_comparison: ReferenceComparisonDto? = null,
    val priority_regions: List<PriorityRegionDto> = emptyList(),
    val model_performance_metadata: ModelPerformanceMetadataDto? = null,
    val stage_durations_ms: Map<String, Double> = emptyMap(),
    val reproducibility: ReproducibilityDto? = null,
    val evidence_trace: Map<String, Any?> = emptyMap(),
    val visualizations: Map<String, String> = emptyMap(),
    val explanation: ExplanationDto? = null,
    val limitations: List<String> = emptyList()
)

data class ImageQualityDto(
    val passed: Boolean = true,
    val resolution: String? = null,
    val laplacian_variance: Double = 0.0,
    val blur_status: String = "ACCEPTABLE",
    val mean_brightness: Double = 0.0,
    val contrast_std: Double = 0.0,
    val mean_saturation: Double = 0.0
)

data class DigepathMetaDto(
    val model_name: String = "Digepath",
    val architecture: String = "ViT-L/16",
    val embedding_dimension: Int = 1024,
    val device: String = "cpu",
    val status: String = "active"
)

data class PredictionDto(
    val `class`: String = "UNKNOWN",
    val confidence: Double = 0.0,
    val calibrated_confidence: Double = 0.0,
    val tumor_probability: Double = 0.0,
    val binary_class: String = "NON-TUM",
    val multiclass_probabilities: Map<String, Double> = emptyMap()
)

data class UncertaintyDto(
    val score: Double = 0.0,
    val level: String = "LOW",
    val entropy: Double = 0.0,
    val normalized_entropy: Double = 0.0,
    val ood_score: Double = 0.0,
    val ood_status: String = "IN_DISTRIBUTION",
    val is_ood: Boolean = false,
    val review_required: Boolean = false,
    val message: String = ""
)

data class ModelAgreementDto(
    val level: String = "HIGH",
    val score: Double = 1.0,
    val concordant_sources: List<String> = emptyList(),
    val discordant_sources: List<String> = emptyList(),
    val summary: String = ""
)

data class NuclearEvidenceDto(
    val total_count: Int = 0,
    val type_counts: Map<String, Int> = emptyMap(),
    val mean_area_px2: Double = 0.0,
    val mean_perimeter_px: Double = 0.0,
    val mean_eccentricity: Double = 0.0,
    val mean_circularity: Double = 0.0,
    val interpretation: String = ""
)

data class GlandEvidenceDto(
    val total_count: Int = 0,
    val mean_area_pixels: Double = 0.0,
    val mean_perimeter_pixels: Double = 0.0,
    val mean_width_pixels: Double = 0.0,
    val mean_height_pixels: Double = 0.0,
    val mean_aspect_ratio: Double = 0.0,
    val mean_circularity: Double = 0.0,
    val interpretation: String = ""
)

data class ReferenceComparisonDto(
    val label: String = "REFERENCE-BASED INSIGHT",
    val is_available: Boolean = true,
    val retrieval_engine: String = "Local In-Memory Vector Search Engine",
    val active_database: String = "outputs/reference_cases",
    val top_category: String = "normal",
    val top_similarity_percent: Double = 0.0,
    val top_reference_id: String = "none",
    val insight: String = "",
    val comparisons: List<ReferenceMatchItemDto> = emptyList()
)

data class ReferenceMatchItemDto(
    val reference_id: String = "",
    val category: String = "",
    val similarity_percent: Double = 0.0,
    val normalized_distance: Double = 0.0,
    val key_concordant_features: List<String> = emptyList()
)

data class PriorityRegionDto(
    val region_id: String,
    val index: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val prediction: String = "UNKNOWN",
    val confidence: Double = 0.0,
    val tumor_probability: Double = 0.0,
    val uncertainty_score: Double = 0.0,
    val uncertainty_level: String = "LOW",
    val priority_score: Double = 0.0,
    val priority_level: String = "LOW",
    val priority_label: String = "",
    val nuclei_count: Int = 0,
    val glands_count: Int = 0,
    val agreement_level: String = "HIGH",
    val rationale: String = ""
)

data class ModelPerformanceMetadataDto(
    val evaluation_dataset: String = "NCT-CRC-HE-100K (45 test patches)",
    val multiclass_accuracy: Double = 0.6444,
    val multiclass_macro_f1: Double = 0.5041,
    val binary_tumor_accuracy: Double = 1.0,
    val expected_calibration_error_ece: Double = 0.1570,
    val verified_date: String = "2026-09-01"
)

data class ReproducibilityDto(
    val pipeline_version: String = "2.0.0",
    val input_image_sha256: String = "",
    val input_image_name: String = "",
    val models: Map<String, String> = emptyMap(),
    val temperature_scaling_factor: Double = 1.25,
    val timestamp_utc: String? = null
)

data class ExplanationClaimDto(
    val claim_id: String = "",
    val category: String = "",
    val claim_statement: String = "",
    val evidence_source: String = "",
    val evidence_value: Any? = null,
    val support_type: String = ""
)

data class ExplanationDto(
    val text: String = "",
    val claims: List<ExplanationClaimDto> = emptyList(),
    val validated: Boolean = true,
    val validation_errors: List<String> = emptyList()
)

data class CopilotAnswerDto(
    val answer: String = "",
    val case_id: String = "",
    val question: String = "",
    val selected_region_id: String? = null,
    val model: String = "Google MedGemma 1.5 4B IT",
    val validated: Boolean = true,
    val validation_errors: List<String> = emptyList(),
    val sources: List<String> = emptyList()
)

data class CaseSummaryItemDto(
    val case_id: String,
    val timestamp: String,
    val prediction_class: String? = null,
    val confidence: Double? = null,
    val uncertainty_level: String? = null,
    val review_status: String = "PENDING"
)

data class ApiErrorDto(
    val error_code: String = "UNKNOWN_ERROR",
    val message: String = "An error occurred during pipeline execution.",
    val case_id: String? = null,
    val stage: String? = null,
    val retryable: Boolean = false
)
