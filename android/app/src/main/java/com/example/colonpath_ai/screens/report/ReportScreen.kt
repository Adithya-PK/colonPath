package com.example.colonpath_ai.screens.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.colonpath_ai.R
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
    
    val effectiveCaseId = caseId?.takeIf { it.isNotBlank() }
        ?: com.example.colonpath_ai.data.ColonPathRepository.activeCaseId
        ?: SampleDataRepository.activeCaseId
        ?: com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult?.case_id
        ?: "COL-2026-001"
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
            val file = File(context.getExternalFilesDir(null), "colonpath_analysis_table_${effectiveCaseId}.csv")
            file.writeText(sb.toString())
            shareFile(context, file, "text/csv")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generatePdfReport(context: Context): Boolean {
        val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
        val caseResultToReport = if (repoCase != null && repoCase.case_id == effectiveCaseId) {
            repoCase
        } else {
            com.example.colonpath_ai.data.ColonPathRepository.casesList.find { it.case_id == effectiveCaseId }
                ?: if (repoCase != null && (caseId.isNullOrBlank() || repoCase.case_id == caseId)) repoCase
                else com.example.colonpath_ai.network.CaseResultDto(
                    case_id = effectiveCaseId,
                    timestamp = "Now",
                    status = "COMPLETED"
                )
        }
        val file = PdfReportGenerator.generateCaseReportPdf(
            context = context,
            caseResult = caseResultToReport,
            specimenBitmap = if (caseResultToReport.case_id == com.example.colonpath_ai.data.ColonPathRepository.activeCaseId) com.example.colonpath_ai.data.ColonPathRepository.selectedBitmap else null
        )
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
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val isReal = repoCase != null
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
                                    text = if (isReal) "Real AI Case Report" else "Prototype Report",
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
                            text = "Generated for Case: $effectiveCaseId • ${repoCase?.timestamp ?: "Real Specimen Inference"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurfaceWhite.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // 1. Patient / Case Information
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                ReportSectionCard(
                    title = "Patient / Case Information",
                    badge = repoCase?.status?.uppercase() ?: analysisResult.case.status.name.replace("_", " ")
                ) {
                    ReportDetailRow("Case ID", effectiveCaseId)
                    ReportDetailRow("Patient ID", analysisResult.case.patient.patientId)
                    ReportDetailRow("Tissue Origin", "Colorectal Mucosa")
                    ReportDetailRow("Specimen Type", "Biopsy Specimen")
                    ReportDetailRow("Staining Protocol", "Hematoxylin & Eosin (H&E)")
                    ReportDetailRow("Lifecycle State", repoCase?.lifecycle_state ?: "COMPLETED")
                }
            }

            // 2. Image Information & Optical Quality
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val q = repoCase?.image_quality
                ReportSectionCard(title = "Optical Quality Assessment", badge = q?.blur_status ?: "PASSED") {
                    ReportDetailRow("Resolution", q?.resolution ?: "${analysisResult.imageInfo.width} × ${analysisResult.imageInfo.height} px")
                    ReportDetailRow("Blur Status", q?.blur_status ?: "ACCEPTABLE")
                    ReportDetailRow("Quality Accepted", if (q?.passed != false) "Yes (Pass)" else "No (Fail)")
                    if (q != null && q.laplacian_variance > 0) {
                        ReportDetailRow("Laplacian Variance", "${String.format("%.1f", q.laplacian_variance)}")
                        ReportDetailRow("Mean Brightness", "${String.format("%.1f", q.mean_brightness)}")
                        ReportDetailRow("Contrast StdDev", "${String.format("%.1f", q.contrast_std)}")
                    }
                }
            }

            // 3. AI Multimodal Prediction Card
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val pred = repoCase?.prediction
                val conf = (pred?.calibrated_confidence ?: pred?.confidence ?: 0.0) * 100.0
                val tumorProb = (pred?.tumor_probability ?: 0.0) * 100.0
                ReportSectionCard(title = "AI Multimodal Prediction", badge = pred?.`class` ?: "EVALUATED") {
                    ReportDetailRow("Predicted Tissue Class", pred?.`class` ?: "Lymphocytes (LYM)")
                    ReportDetailRow("Calibrated Confidence", "${String.format("%.1f", conf)}%")
                    ReportDetailRow("Binary Tumor Likelihood", "${String.format("%.1f", tumorProb)}%")
                    ReportDetailRow("Foundation Model", repoCase?.digepath?.architecture ?: "Phikon-v2 ViT-L/16 via DINOv2")
                    ReportDetailRow("Multimodal Bottleneck", "1024-d Visual + 16-d Morphology -> 128-d Latent")
                }
            }

            // 4. Nuclear Morphometry Findings (Real HoVer-Net)
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val nuc = repoCase?.nuclear_evidence
                val count = nuc?.total_count ?: analysisResult.nuclearAnalysis.nucleiDetected
                ReportSectionCard(
                    title = "Nuclear Morphometry (HoVer-Net)",
                    badge = "$count Nuclei"
                ) {
                    ReportDetailRow("Total Nuclei Detected", "$count")
                    if (nuc != null) {
                        ReportDetailRow("Mean Nuclear Area", "${String.format("%.1f", nuc.mean_area_px2)} px²")
                        ReportDetailRow("Mean Perimeter", "${String.format("%.1f", nuc.mean_perimeter_px)} px")
                        ReportDetailRow("Nuclear Circularity", "${String.format("%.3f", nuc.mean_circularity)}")
                        ReportDetailRow("Nuclear Eccentricity", "${String.format("%.3f", nuc.mean_eccentricity)}")
                        if (nuc.type_counts.isNotEmpty()) {
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                            Text(
                                text = "Cell Population: Spindle: ${nuc.type_counts["spindle_shaped"] ?: 0}, Epithelial: ${nuc.type_counts["epithelial"] ?: 0}, Misc: ${nuc.type_counts["miscellaneous"] ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        ReportDetailRow("Nuclear Density", "${analysisResult.nuclearAnalysis.nuclearDensity} /mm²")
                        ReportDetailRow("Mean Nuclear Area", "${analysisResult.nuclearAnalysis.meanNuclearArea} px²")
                        ReportDetailRow("Nuclear Circularity", "${analysisResult.nuclearAnalysis.nuclearCircularity}")
                    }
                }
            }

            // 5. Gland Architecture Findings (Real U-Net)
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val gland = repoCase?.gland_evidence
                val gCount = gland?.total_count ?: analysisResult.glandAnalysis.glandCount
                ReportSectionCard(
                    title = "Glandular Architecture (U-Net)",
                    badge = "$gCount Glands"
                ) {
                    ReportDetailRow("Total Glands Segmented", "$gCount")
                    if (gland != null) {
                        ReportDetailRow("Mean Gland Area", "${String.format("%.1f", gland.mean_area_pixels)} px²")
                        ReportDetailRow("Mean Gland Perimeter", "${String.format("%.1f", gland.mean_perimeter_pixels)} px")
                        ReportDetailRow("Gland Circularity", "${String.format("%.3f", gland.mean_circularity)}")
                        ReportDetailRow("Gland Aspect Ratio", "${String.format("%.2f", gland.mean_aspect_ratio)}")
                        ReportDetailRow("Mean Dimensions (W × H)", "${String.format("%.1f", gland.mean_width_pixels)} × ${String.format("%.1f", gland.mean_height_pixels)} px")
                    } else {
                        ReportDetailRow("Mean Gland Area", "${analysisResult.glandAnalysis.meanGlandArea} px²")
                        ReportDetailRow("Mean Gland Perimeter", "${analysisResult.glandAnalysis.meanGlandPerimeter} px")
                    }
                }
            }

            // 6. Model Uncertainty & Multi-Source Consensus
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val unc = repoCase?.uncertainty
                val agr = repoCase?.model_agreement
                ReportSectionCard(title = "Model Uncertainty & Consensus") {
                    ReportDetailRow("Uncertainty Level", unc?.level ?: "LOW")
                    ReportDetailRow("Shannon Entropy", "${String.format("%.4f", unc?.entropy ?: 0.0)}")
                    ReportDetailRow("OOD Distribution", unc?.ood_status ?: "IN_DISTRIBUTION")
                    ReportDetailRow("Consensus Agreement", agr?.level ?: "MEDIUM")
                    if (!agr?.summary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(agr?.summary ?: "", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }

            // 7. Clinical Grounded Explanation
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                val expl = repoCase?.explanation
                ReportSectionCard(title = "AI-Assisted Interpretation") {
                    Text(
                        text = expl?.text ?: analysisResult.aiReport.interpretation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    if (expl?.claims?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Validated Clinical Claims:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        expl.claims.forEach { claim ->
                            Text("• ${claim.claim_statement}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // 8. Limitations & Guidelines
            item {
                val repoCase = com.example.colonpath_ai.data.ColonPathRepository.currentCaseResult
                ReportSectionCard(title = "Limitations & Protocol Guidelines") {
                    val lims = repoCase?.limitations?.takeIf { it.isNotEmpty() } ?: analysisResult.aiReport.limitations.items
                    lims.forEach { limitation ->
                        Text(
                            text = "• $limitation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
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
                Button(
                    onClick = {
                        val success = exportAnalysisTableCsv(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "CSV exported successfully! Opened share/view sheet."
                                else "Failed to export CSV."
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue50,
                        contentColor = Blue500
                    ),
                    border = BorderStroke(1.dp, Blue500.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TableChart,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download Analysis Table (CSV)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Blue500
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
