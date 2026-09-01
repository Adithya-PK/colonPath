package com.example.colonpath_ai.model

enum class QualityStatus { GOOD, NEEDS_REVIEW, REJECTED }

data class ImageInfo(
    val imageId: String,
    val width: Int,
    val height: Int,
    val source: String,
    val magnification: String = "Not available",
    val calibrationStatus: String = "Calibration unavailable"
)

data class ImageQuality(
    val status: QualityStatus,
    val resolution: String,
    val dimensions: String,
    val blurStatus: String,
    val calibrationStatus: String,
    val accepted: Boolean
)
