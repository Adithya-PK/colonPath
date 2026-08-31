package com.example.colonpath_ai.screens.comparison

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onBack: () -> Unit,
    onReport: () -> Unit
) {
    val caseId = SampleDataRepository.activeCaseId ?: "COL-2026-001"
    val analysisResult = SampleDataRepository.getAnalysisForCase(caseId)

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
            // Prototype Status Badge
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberLight
                ) {
                    Text(
                        text = "Demo Analysis • Illustrative Reference Matches",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Retrieved Reference Cases Section
            item {
                SectionHeader(
                    title = "Retrieved Reference Cases",
                    subtitle = "Historical cases with nearest histological feature vectors"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    analysisResult.referenceComparison.references.forEach { ref ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ref.referenceId,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Blue50
                                    ) {
                                        Text(
                                            text = "Similarity: ${ref.similarityScore}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Blue500,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = ref.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )

                                if (ref.relevantMetrics.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ref.relevantMetrics.forEach { metricName ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = BackgroundLight
                                            ) {
                                                Text(
                                                    text = metricName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reference vs Patient Comparison Table Section
            item {
                SectionHeader(
                    title = "Reference vs Patient Comparison",
                    subtitle = "Quantitative morphological measurements against baseline"
                )
                Spacer(modifier = Modifier.height(6.dp))
                ComparisonTable(
                    metrics = analysisResult.referenceComparison.metrics,
                    referenceHeader = "Reference Baseline",
                    patientHeader = "Patient (${analysisResult.case.caseId})"
                )
            }

            // Why This Result Expandable Section
            item {
                var expanded by remember { mutableStateOf(false) }
                SectionHeader(
                    title = "Why This Result?",
                    subtitle = "Computational rationale and similarity explanation",
                    expandable = true,
                    expanded = expanded,
                    onToggle = { expanded = !expanded }
                )
                if (expanded) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Supporting Computational Observations",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "• Nuclear density (${analysisResult.nuclearAnalysis.nuclearDensity}/mm²) and circularity (${analysisResult.nuclearAnalysis.nuclearCircularity}) align closely with adenomatous tissue morphology.\n• Glandular boundary irregularity (${analysisResult.glandAnalysis.boundaryIrregularity}) exhibits moderate architectural distortion consistent with reference case ${analysisResult.referenceComparison.references.firstOrNull()?.referenceId ?: "REF-021"}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            Text(
                                text = "Retrieval Methodology",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "• High-dimensional vision embeddings are mapped against a verified histopathology reference index using cosine similarity distance.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            Text(
                                text = "Important Limitations",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "• Similarity score represents computational feature alignment and does NOT constitute a clinical diagnosis.\n• All findings must be independently confirmed by a certified pathologist.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Clinical Retrieval Disclaimer Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberLight),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Computational Note: Similarity scores represent feature-space vector proximity to reference archives and are not diagnostic disease probabilities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Bottom Action
            item {
                Button(
                    onClick = onReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View AI-Assisted Report")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
