package com.example.colonpath_ai.screens.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun MorphologyScreen(onBack: () -> Unit) {
    val caseResult = ColonPathRepository.currentCaseResult
    val caseId = caseResult?.case_id ?: ColonPathRepository.activeCaseId ?: "UNKNOWN_CASE"
    val nuc = caseResult?.nuclear_evidence
    val gland = caseResult?.gland_evidence

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Morphometry Evidence") },
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Case ID: $caseId", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (caseResult != null) "Quantitative morphometry dynamically extracted from U-Net & HoVer-Net."
                            else "No active case loaded. Please run analysis on an H&E image to compute morphometry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 1. Nuclear Morphometry
            item {
                SectionHeader("Nuclear Morphometry (HoVer-Net)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Total Nuclei", "${nuc?.total_count ?: 0}", modifier = Modifier.weight(1f))
                    MetricCard("Mean Area", "${String.format("%.1f", nuc?.mean_area_px2 ?: 0.0)}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Perimeter", "${String.format("%.1f", nuc?.mean_perimeter_px ?: 0.0)}", "px", modifier = Modifier.weight(1f))
                    MetricCard("Circularity", "${String.format("%.3f", nuc?.mean_circularity ?: 0.0)}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Eccentricity", "${String.format("%.3f", nuc?.mean_eccentricity ?: 0.0)}", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Nuclear Subtype Breakdown
                if (nuc?.type_counts?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Cellular Population Breakdown (HoVer-Net CoNSeP)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            val total = nuc.total_count.coerceAtLeast(1)
                            nuc.type_counts.forEach { (type, count) ->
                                val pct = (count.toDouble() / total.toDouble()) * 100.0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(type.replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("$count cells (${String.format("%.1f", pct)}%)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                if (!nuc?.interpretation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue50),
                        border = BorderStroke(1.dp, Blue100)
                    ) {
                        Text(
                            text = "Nuclear Finding: ${nuc?.interpretation}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy800
                        )
                    }
                }
            }

            // 2. Glandular Architecture Morphometry
            item {
                SectionHeader("Gland Architecture (U-Net)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Gland Count", "${gland?.total_count ?: 0}", modifier = Modifier.weight(1f))
                    MetricCard("Mean Gland Area", "${String.format("%.1f", gland?.mean_area_pixels ?: 0.0)}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Perimeter", "${String.format("%.1f", gland?.mean_perimeter_pixels ?: 0.0)}", "px", modifier = Modifier.weight(1f))
                    MetricCard("Circularity", "${String.format("%.3f", gland?.mean_circularity ?: 0.0)}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Aspect Ratio", "${String.format("%.2f", gland?.mean_aspect_ratio ?: 0.0)}", modifier = Modifier.weight(1f))
                    MetricCard("Mean Width", "${String.format("%.1f", gland?.mean_width_pixels ?: 0.0)}", "px", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Mean Height", "${String.format("%.1f", gland?.mean_height_pixels ?: 0.0)}", "px", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!gland?.interpretation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue50),
                        border = BorderStroke(1.dp, Blue100)
                    ) {
                        Text(
                            text = "Gland Finding: ${gland?.interpretation}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy800
                        )
                    }
                }
            }

            // 3. Extended Morphometry Status Card
            item {
                SectionHeader("Extended Morphometry Status")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Architectural Crowding", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Derived from 16-D Feature Vector", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Branching Index", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Not evaluated by single-tile pass", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Glandular Density (/mm²)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Not evaluated by single-tile pass", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                    }
                }
            }

            // 4. Clinical Research Disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        "Morphometry metrics represent quantitative computational measurements derived from U-Net & HoVer-Net deep models. Decision-support output for research use; requires qualified pathologist review.",
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
