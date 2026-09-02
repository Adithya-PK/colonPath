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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.SectionHeader
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onBack: () -> Unit,
    onReport: () -> Unit
) {
    val caseResult = ColonPathRepository.currentCaseResult
    val caseId = caseResult?.case_id ?: ColonPathRepository.activeCaseId ?: "COL-2026-013"
    val nuc = caseResult?.nuclear_evidence
    val gland = caseResult?.gland_evidence

    val nucCount = nuc?.total_count ?: 182
    val nucArea = nuc?.mean_area_px2 ?: 47.3
    val nucCirc = nuc?.mean_circularity ?: 0.72

    val glandCount = gland?.total_count ?: 18
    val glandArea = gland?.mean_area_pixels ?: 2840.0
    val glandCirc = gland?.mean_circularity ?: 0.68

    var isWhyResultExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Reference Comparison",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
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
            // 1. Retrieved Reference Cases
            item {
                SectionHeader(
                    title = "Retrieved Reference Cases",
                    subtitle = "Historical cases with nearest histological feature vectors"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReferenceCaseCard(
                        caseId = "REF-021",
                        similarity = 94.2,
                        description = "Adenoma-like morphology",
                        tags = listOf("Nuclear density", "Gland irregularity", "Circularity")
                    )
                    ReferenceCaseCard(
                        caseId = "REF-034",
                        similarity = 91.8,
                        description = "Adenocarcinoma-like morphology",
                        tags = listOf("Nuclear area", "Crowding", "Shape")
                    )
                    ReferenceCaseCard(
                        caseId = "REF-011",
                        similarity = 88.6,
                        description = "Adenomatous morphology",
                        tags = listOf("Gland density", "Nuclear morphology")
                    )
                }
            }

            // 2. Reference vs Patient Comparison Table (Normalized Per-Unit Basis)
            item {
                SectionHeader(
                    title = "Reference vs Patient Comparison",
                    subtitle = "Quantitative morphological measurements against normalized baseline"
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Blue50.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Metric", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = TextPrimary, modifier = Modifier.weight(1.2f))
                            Text("Reference\nBaseline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text("Patient\n($caseId)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Blue500, modifier = Modifier.weight(1f))
                        }

                        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        val rows = listOf(
                            Triple("Nuclei Count / Patch", "~180 cells", "$nucCount cells"),
                            Triple("Nuclear Density", "98.5 /mm²", "${String.format("%.1f", nucCount * 0.076)} /mm²"),
                            Triple("Mean Nuclear Area", "38.6 px²", "${String.format("%.1f", nucArea)} px²"),
                            Triple("Nuclear Circularity", "0.86", "${String.format("%.2f", nucCirc)}"),
                            Triple("Gland Count / Patch", "~16 glands", "$glandCount glands"),
                            Triple("Gland Density", "12.8 /mm²", "${String.format("%.1f", glandCount * 0.076)} /mm²"),
                            Triple("Mean Gland Area", "3,420 px²", "${String.format("%.0f", glandArea)} px²"),
                            Triple("Gland Irregularity", "0.31", "${String.format("%.2f", 1.0 - glandCirc)}")
                        )

                        rows.forEachIndexed { index, (metric, refVal, patientVal) ->
                            val bg = if (index % 2 == 0) SurfaceWhite else BackgroundLight.copy(alpha = 0.5f)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(metric, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1.2f))
                                Text(refVal, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                Text(patientVal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                            if (index < rows.size - 1) {
                                HorizontalDivider(color = CardBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // 3. Why This Result?
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Why This Result?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Computational rationale and similarity explanation", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            IconButton(onClick = { isWhyResultExpanded = !isWhyResultExpanded }) {
                                Icon(
                                    if (isWhyResultExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Toggle Rationale"
                                )
                            }
                        }

                        if (isWhyResultExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The feature space embedding exhibits nuclear pleomorphism and glandular boundary irregularity consistent with historical reference profiles. Proximity indicates morphological features aligned with adenomatous/tubular architecture.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Computational Note Amber Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AmberLight
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Computational Note: Baseline metrics are normalized to standardized optical fields. Similarity scores represent feature-space vector proximity, not definitive diagnostic likelihood.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            // 5. Action Button: View AI-Assisted Report
            item {
                Button(
                    onClick = onReport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                ) {
                    Text("View AI-Assisted Report")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ReferenceCaseCard(
    caseId: String,
    similarity: Double,
    description: String,
    tags: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(caseId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Surface(shape = RoundedCornerShape(8.dp), color = Blue50) {
                    Text(
                        "Similarity: ${String.format("%.1f", similarity)}%",
                        color = Blue500,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(6.dp), color = BackgroundLight) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
