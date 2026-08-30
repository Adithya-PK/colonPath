package com.example.colonpath_ai.model

data class NuclearAnalysis(
    val nucleiDetected: Int,
    val nuclearDensity: Double,
    val meanNuclearArea: Double,
    val medianNuclearArea: Double,
    val nuclearCircularity: Double,
    val eccentricity: Double,
    val aspectRatio: Double,
    val nearestNeighborDistance: Double
)

data class CellCategory(
    val name: String,
    val count: Int,
    val percentage: Double
)

data class NuclearClassification(
    val categories: List<CellCategory>
)
