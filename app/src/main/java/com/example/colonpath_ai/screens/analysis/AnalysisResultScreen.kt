package com.example.colonpath_ai.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    onBack: () -> Unit,
    onMorphology: () -> Unit,
    onComparison: () -> Unit,
    onReport: () -> Unit
) {
    var selectedImageMode by remember { mutableStateOf("Overlay") }
    var isCaseSaved by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val caseId = SampleDataRepository.activeCaseId ?: "COL-2026-001"
    val analysisResult = SampleDataRepository.getAnalysisForCase(caseId)
    val case = analysisResult.case

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analysis Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Blue50)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Case ID: ${case.caseId}", style = MaterialTheme.typography.titleMedium)
                            StatusBadge(status = case.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Patient: ${case.patient.patientName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Tissue: ${case.tissue}", style = MaterialTheme.typography.bodyMedium)
                        Text("Stain: ${case.stain}", style = MaterialTheme.typography.bodyMedium)
                        Text("Date: ${case.analysisDate}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge(containerColor = AmberLight) {
                            Text("Demo Analysis", color = TextPrimary)
                        }
                    }
                }
            }

            item {
                SectionHeader("Image Quality")
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        QualityBadge(status = analysisResult.imageQuality.status)
                        Text("Resolution: ${analysisResult.imageQuality.resolution}")
                        Text("Blur: ${analysisResult.imageQuality.blurStatus}")
                        Text("Calibration: ${analysisResult.imageQuality.calibrationStatus}")
                        Text("Accepted: ${if (analysisResult.imageQuality.accepted) "Yes" else "No"}")
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Analyzed Specimen & AI Overlays",
                    subtitle = "Interactive multi-layer view of detected histology features"
                )
                Spacer(modifier = Modifier.height(4.dp))
                ImageViewer(
                    selectedMode = selectedImageMode,
                    onModeChange = { selectedImageMode = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionHeader("Nuclear Analysis")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Nuclei Detected", "${analysisResult.nuclearAnalysis.nucleiDetected}", modifier = Modifier.weight(1f))
                    MetricCard("Nuclear Density", "${analysisResult.nuclearAnalysis.nuclearDensity}", "/mm²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Area", "${analysisResult.nuclearAnalysis.meanNuclearArea}", "px²", modifier = Modifier.weight(1f))
                    MetricCard("Median Area", "${analysisResult.nuclearAnalysis.medianNuclearArea}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Circularity", "${analysisResult.nuclearAnalysis.nuclearCircularity}", modifier = Modifier.weight(1f))
                    MetricCard("Eccentricity", "${analysisResult.nuclearAnalysis.eccentricity}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Aspect Ratio", "${analysisResult.nuclearAnalysis.aspectRatio}", modifier = Modifier.weight(1f))
                    MetricCard("NN Distance", "${analysisResult.nuclearAnalysis.nearestNeighborDistance}", "px", modifier = Modifier.weight(1f))
                }
            }

            item {
                SectionHeader("Nuclear Classification")
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        analysisResult.nuclearClassification.categories.forEach { cat ->
                            Text("${cat.name} ${cat.count} (${cat.percentage}%)")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Note: Cell categories represent computational classification, not clinical diagnosis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                SectionHeader("Gland Analysis")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Gland Count", "${analysisResult.glandAnalysis.glandCount}", modifier = Modifier.weight(1f))
                    MetricCard("Mean Area", "${analysisResult.glandAnalysis.meanGlandArea}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Perimeter", "${analysisResult.glandAnalysis.meanGlandPerimeter}", "px", modifier = Modifier.weight(1f))
                    MetricCard("Spacing", "${analysisResult.glandAnalysis.glandSpacing}", "px", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Density", "${analysisResult.glandAnalysis.glandDensity}", "/mm²", modifier = Modifier.weight(1f))
                    MetricCard("Shape", "${analysisResult.glandAnalysis.glandShape}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Branching", analysisResult.glandAnalysis.branching, modifier = Modifier.weight(1f))
                    MetricCard("Irregularity", "${analysisResult.glandAnalysis.boundaryIrregularity}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Crowding", analysisResult.glandAnalysis.crowding, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                Button(
                    onClick = {
                        val updatedCase = case.copy(status = com.example.colonpath_ai.model.CaseStatus.COMPLETED)
                        SampleDataRepository.addCase(updatedCase)
                        isCaseSaved = true
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Case ${case.caseId} successfully saved to History!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCaseSaved) GreenSuccess else Blue500
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isCaseSaved) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCaseSaved) "Case Saved to History" else "Save Case to History")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onMorphology, modifier = Modifier.weight(1f)) {
                        Text("Morphology")
                    }
                    OutlinedButton(onClick = onComparison, modifier = Modifier.weight(1f)) {
                        Text("Comparison")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                    Text("View Full Report")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
