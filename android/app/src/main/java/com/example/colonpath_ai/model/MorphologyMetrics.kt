package com.example.colonpath_ai.model

data class NuclearMorphology(
    val meanArea: Double,
    val medianArea: Double,
    val areaStdDev: Double,
    val meanCircularity: Double,
    val meanEccentricity: Double,
    val meanAspectRatio: Double,
    val pleomorphismIndex: Double
)

data class GlandMorphology(
    val meanArea: Double,
    val areaVariance: Double,
    val meanPerimeter: Double,
    val meanCircularity: Double,
    val irregularityIndex: Double,
    val crowdingScore: Double,
    val branchingFrequency: Double
)

data class MorphologyMetrics(
    val nuclear: NuclearMorphology,
    val gland: GlandMorphology
)
