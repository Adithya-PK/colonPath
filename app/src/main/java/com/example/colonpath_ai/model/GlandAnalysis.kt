package com.example.colonpath_ai.model

data class GlandAnalysis(
    val glandCount: Int,
    val meanGlandArea: Double,
    val meanGlandPerimeter: Double,
    val glandSpacing: Double,
    val glandDensity: Double,
    val glandShape: Double,
    val branching: String,
    val boundaryIrregularity: Double,
    val crowding: String
)
