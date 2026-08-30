package com.example.colonpath_ai.model

data class AnalysisResult(
    val case: Case,
    val imageInfo: ImageInfo,
    val imageQuality: ImageQuality,
    val nuclearAnalysis: NuclearAnalysis,
    val nuclearClassification: NuclearClassification,
    val glandAnalysis: GlandAnalysis,
    val morphologyMetrics: MorphologyMetrics,
    val referenceComparison: ReferenceComparison,
    val aiReport: AIReport
)
