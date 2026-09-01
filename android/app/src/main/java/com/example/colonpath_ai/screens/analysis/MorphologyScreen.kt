package com.example.colonpath_ai.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphologyScreen(onBack: () -> Unit) {
    val caseId = com.example.colonpath_ai.data.SampleDataRepository.activeCaseId ?: "COL-2026-001"
    val analysisResult = com.example.colonpath_ai.data.SampleDataRepository.getAnalysisForCase(caseId)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Morphology Analysis") },
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
                Badge(containerColor = AmberLight) {
                    Text("Demo Analysis", color = TextPrimary)
                }
            }

            item {
                SectionHeader("Nuclear Morphology")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Area", "${analysisResult.morphologyMetrics.nuclear.meanArea}", "px²", modifier = Modifier.weight(1f))
                    MetricCard("Median Area", "${analysisResult.morphologyMetrics.nuclear.medianArea}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Area Std Dev", "${analysisResult.morphologyMetrics.nuclear.areaStdDev}", modifier = Modifier.weight(1f))
                    MetricCard("Circularity", "${analysisResult.morphologyMetrics.nuclear.meanCircularity}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Eccentricity", "${analysisResult.morphologyMetrics.nuclear.meanEccentricity}", modifier = Modifier.weight(1f))
                    MetricCard("Aspect Ratio", "${analysisResult.morphologyMetrics.nuclear.meanAspectRatio}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Pleomorphism Index", "${analysisResult.morphologyMetrics.nuclear.pleomorphismIndex}", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                SectionHeader("Gland Morphology")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Area", "${analysisResult.morphologyMetrics.gland.meanArea}", "px²", modifier = Modifier.weight(1f))
                    MetricCard("Area Variance", "${analysisResult.morphologyMetrics.gland.areaVariance}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Perimeter", "${analysisResult.morphologyMetrics.gland.meanPerimeter}", "px", modifier = Modifier.weight(1f))
                    MetricCard("Circularity", "${analysisResult.morphologyMetrics.gland.meanCircularity}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Irregularity Index", "${analysisResult.morphologyMetrics.gland.irregularityIndex}", modifier = Modifier.weight(1f))
                    MetricCard("Crowding Score", "${analysisResult.morphologyMetrics.gland.crowdingScore}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Branching Freq", "${analysisResult.morphologyMetrics.gland.branchingFrequency}", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Text(
                        "Morphology metrics represent quantitative measurements. Clinical significance should be assessed by a pathologist.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
