package com.example.colonpath_ai.screens.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*
import com.example.colonpath_ai.util.PdfReportGenerator
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ReportSectionCard(
    title: String,
    badge: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                if (!badge.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Blue50
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Blue500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
            content()
        }
    }
}

@Composable
fun ReportDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    caseId: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val effectiveCaseId = caseId ?: SampleDataRepository.activeCaseId ?: "COL-2026-001"
    val analysisResult = SampleDataRepository.getAnalysisForCase(effectiveCaseId)

    fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open or Share Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAnalysisTableCsv(context: Context): Boolean {
        return try {
            val sb = StringBuilder()
            sb.appendLine("Metric,Reference Baseline,Patient Specimen")
            analysisResult.referenceComparison.metrics.forEach { m ->
                sb.appendLine("\"${m.name}\",\"${m.referenceValue}\",\"${m.patientValue}\"")
            }
            val file = File(context.getExternalFilesDir(null), "colonpath_analysis_table_${analysisResult.case.caseId}.csv")
            file.writeText(sb.toString())
            shareFile(context, file, "text/csv")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generatePdfReport(context: Context): Boolean {
        val file = PdfReportGenerator.generateReportPdf(context, analysisResult)
        return if (file != null) {
            shareFile(context, file, "application/pdf")
            true
        } else {
            false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI-Assisted Report") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Report Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ColonPath-AI",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = SurfaceWhite
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceWhite.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Prototype Report",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Computational Colorectal Histopathology Evaluation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SurfaceWhite.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Generated on ${analysisResult.case.analysisDate} • Illustrative Demo Findings",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurfaceWhite.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // 1. Patient / Case Information
            item {
                ReportSectionCard(
                    title = "Patient / Case Information",
                    badge = analysisResult.case.status.name.replace("_", " ")
                ) {
                    ReportDetailRow("Case ID", analysisResult.case.caseId)
                    ReportDetailRow("Patient ID", analysisResult.case.patient.patientId)
                    ReportDetailRow("Patient Name", analysisResult.case.patient.patientName)
                    ReportDetailRow("Tissue Origin", analysisResult.case.tissue)
                    ReportDetailRow("Specimen Type", analysisResult.case.sampleType)
                    ReportDetailRow("Staining Protocol", analysisResult.case.stain)
                    if (analysisResult.case.notes.isNotEmpty()) {
                        HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                        Text(
                            text = "Clinical Notes: ${analysisResult.case.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 2. Image Information
            item {
                ReportSectionCard(title = "Image Information") {
                    ReportDetailRow("Image Identifier", analysisResult.imageInfo.imageId)
                    ReportDetailRow("Dimensions", "${analysisResult.imageInfo.width} × ${analysisResult.imageInfo.height} px")
                    ReportDetailRow("Acquisition Source", analysisResult.imageInfo.source)
                    ReportDetailRow("Calibration", analysisResult.imageInfo.calibrationStatus)
                }
            }

            // 3. Image Quality (Unified full-width card with matching margins & padding)
            item {
                ReportSectionCard(
                    title = "Image Quality Assessment",
                    badge = analysisResult.imageQuality.status.name
                ) {
                    ReportDetailRow("Quality Status", analysisResult.imageQuality.status.name)
                    ReportDetailRow("Resolution", analysisResult.imageQuality.resolution)
                    ReportDetailRow("Blur Status", analysisResult.imageQuality.blurStatus)
                    ReportDetailRow("Optical Calibration", analysisResult.imageQuality.calibrationStatus)
                    ReportDetailRow("Quality Accepted", if (analysisResult.imageQuality.accepted) "Yes (Pass)" else "No (Fail)")
                }
            }

            // 4. Nuclear Findings
            item {
                ReportSectionCard(
                    title = "Nuclear Findings",
                    badge = "${analysisResult.nuclearAnalysis.nucleiDetected} Nuclei"
                ) {
                    ReportDetailRow("Nuclei Count", "${analysisResult.nuclearAnalysis.nucleiDetected}")
                    ReportDetailRow("Nuclear Density", "${analysisResult.nuclearAnalysis.nuclearDensity} /mm²")
                    ReportDetailRow("Mean Nuclear Area", "${analysisResult.nuclearAnalysis.meanNuclearArea} px²")
                    ReportDetailRow("Nuclear Circularity", "${analysisResult.nuclearAnalysis.nuclearCircularity}")
                    ReportDetailRow("Mean Aspect Ratio", "${analysisResult.nuclearAnalysis.aspectRatio}")
                    
                    val dominantClass = analysisResult.nuclearClassification.categories.maxByOrNull { it.percentage }
                    if (dominantClass != null) {
                        HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                        Text(
                            text = "Dominant Cell Population: ${dominantClass.name} (${dominantClass.percentage}% | ${dominantClass.count} cells)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 5. Gland Findings
            item {
                ReportSectionCard(
                    title = "Gland Findings",
                    badge = "${analysisResult.glandAnalysis.glandCount} Glands"
                ) {
                    ReportDetailRow("Total Glands Detected", "${analysisResult.glandAnalysis.glandCount}")
                    ReportDetailRow("Mean Gland Area", "${analysisResult.glandAnalysis.meanGlandArea} px²")
                    ReportDetailRow("Mean Gland Perimeter", "${analysisResult.glandAnalysis.meanGlandPerimeter} px")
                    ReportDetailRow("Glandular Spacing", "${analysisResult.glandAnalysis.glandSpacing} px")
                    ReportDetailRow("Boundary Irregularity", "${analysisResult.glandAnalysis.boundaryIrregularity}")
                    ReportDetailRow("Architectural Crowding", analysisResult.glandAnalysis.crowding)
                    ReportDetailRow("Branching Pattern", analysisResult.glandAnalysis.branching)
                }
            }

            // 6. Reference Comparison Summary
            item {
                ReportSectionCard(title = "Reference Retrieval Summary") {
                    val topMatch = analysisResult.referenceComparison.references.firstOrNull()
                    if (topMatch != null) {
                        ReportDetailRow("Closest Match ID", topMatch.referenceId)
                        ReportDetailRow("Retrieval Similarity", "${topMatch.similarityScore}%")
                        ReportDetailRow("Reference Classification", topMatch.category)
                    }
                    Text(
                        text = "Similarity scores denote computational feature vector proximity to archived cases, not diagnostic disease likelihood.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 7. AI-Assisted Interpretation
            item {
                ReportSectionCard(title = "AI-Assisted Interpretation") {
                    Text(
                        text = analysisResult.aiReport.interpretation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supporting Evidence:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    analysisResult.aiReport.supportingEvidence.forEach { evidence ->
                        Text(
                            text = "• $evidence",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 8. Limitations & Uncertainty
            item {
                ReportSectionCard(title = "Limitations & Model Uncertainty") {
                    analysisResult.aiReport.limitations.items.forEach { limitation ->
                        Text(
                            text = "• $limitation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uncertainty Note: ${analysisResult.aiReport.uncertainty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 9. Pathologist Review Warning
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RedLight),
                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = RedError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "PATHOLOGIST REVIEW REQUIRED",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = RedError
                            )
                            Text(
                                text = analysisResult.aiReport.pathologistReview.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 10. Action Buttons (PDF & CSV Export)
            item {
                Button(
                    onClick = {
                        val success = generatePdfReport(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Report PDF generated successfully! Opened share/view sheet."
                                else "Failed to generate PDF."
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PDF Report")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val success = exportAnalysisTableCsv(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "CSV exported successfully! Opened share/view sheet."
                                else "Failed to export CSV."
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Analysis Table (CSV)")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
