package com.example.colonpath_ai.screens.comparison

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onBack: () -> Unit,
    onReport: () -> Unit
) {
    val caseResult = ColonPathRepository.currentCaseResult
    val caseId = caseResult?.case_id ?: ColonPathRepository.activeCaseId ?: "UNKNOWN_CASE"
    val refComp = caseResult?.reference_comparison
    val agreement = caseResult?.model_agreement
    val perf = caseResult?.model_performance_metadata

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reference Comparison") },
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
                .background(BackgroundLight)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Case ID: $caseId", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Surface(shape = RoundedCornerShape(8.dp), color = Blue50) {
                                Text(
                                    refComp?.retrieval_engine ?: "Local Vector Engine",
                                    color = Blue500,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (caseResult != null && !refComp?.insight.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(refComp?.insight ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        } else if (caseResult == null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No active case loaded. Select or analyze a case to compare with reference cohorts.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // Top Match Overview
            item {
                SectionHeader(
                    title = "Top Reference Match",
                    subtitle = "Nearest cohort profile in 16D morphology & 1024D embedding space"
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Matched Category", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(refComp?.top_category?.uppercase() ?: "NORMAL", fontWeight = FontWeight.Bold, color = Blue500)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vector Similarity", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${String.format("%.1f", refComp?.top_similarity_percent ?: 0.0)}%", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // Reference Matches List
            item {
                SectionHeader("Retrieved Cohort Matches")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val comps = refComp?.comparisons ?: emptyList()
                    if (comps.isEmpty()) {
                        Text("No external reference records matching criteria.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    } else {
                        comps.forEach { comp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(comp.reference_id, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("${String.format("%.1f", comp.similarity_percent)}% Match", fontWeight = FontWeight.Bold, color = Blue500)
                                    }
                                    Text("Cohort: ${comp.category.uppercase()} • Distance: ${String.format("%.3f", comp.normalized_distance)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    if (comp.key_concordant_features.isNotEmpty()) {
                                        Text("Concordant features: ${comp.key_concordant_features.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Model Consensus Agreement
            item {
                SectionHeader("Multi-Source Consensus Analysis")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Consensus Agreement", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(agreement?.level ?: "HIGH", fontWeight = FontWeight.Bold, color = Blue500)
                        }
                        if (agreement?.concordant_sources?.isNotEmpty() == true) {
                            Text("Concordant Sources: ${agreement.concordant_sources.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = GreenSuccess)
                        }
                        if (agreement?.discordant_sources?.isNotEmpty() == true) {
                            Text("Discordant Sources: ${agreement.discordant_sources.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = RedError)
                        }
                    }
                }
            }

            // Model Performance Metadata
            if (perf != null) {
                item {
                    SectionHeader("Model Performance Metadata")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Benchmark: ${perf.evaluation_dataset}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text("Multiclass Accuracy: ${String.format("%.2f", perf.multiclass_accuracy * 100)}% • Binary Tumor Accuracy: ${String.format("%.1f", perf.binary_tumor_accuracy * 100)}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Macro F1: ${String.format("%.4f", perf.multiclass_macro_f1)} • Calibration ECE: ${String.format("%.4f", perf.expected_calibration_error_ece)}", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                    }
                }
            }

            // Disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        "Reference comparison and similarity metrics provide computational decision support for research purposes only. Not for autonomous diagnostic use.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
