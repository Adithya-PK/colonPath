package com.example.colonpath_ai.screens.report

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.network.ColonPathApiClient
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
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
                        shape = RoundedCornerShape(8.dp),
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
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
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
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
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

    val caseResult = ColonPathRepository.currentCaseResult
    val effectiveCaseId = caseId?.takeIf { it.isNotBlank() }
        ?: caseResult?.case_id
        ?: ColonPathRepository.activeCaseId
        ?: "COL-2026-013"

    val nuc = caseResult?.nuclear_evidence
    val gland = caseResult?.gland_evidence
    val quality = caseResult?.image_quality

    val nucCount = nuc?.total_count ?: 1824
    val nucArea = nuc?.mean_area_px2 ?: 47.3
    val nucCirc = nuc?.mean_circularity ?: 0.72

    val glandCount = gland?.total_count ?: 146
    val glandArea = gland?.mean_area_pixels ?: 2840.0
    val glandPerim = gland?.mean_perimeter_pixels ?: 312.5
    val glandCirc = gland?.mean_circularity ?: 0.68

    var overlayBitmap by remember { mutableStateOf<Bitmap?>(ColonPathRepository.selectedBitmap) }
    
    LaunchedEffect(effectiveCaseId) {
        val bmp = ColonPathApiClient.fetchVisualizationBitmap(effectiveCaseId, "nuclei")
        if (bmp != null) overlayBitmap = bmp
    }

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
            sb.appendLine("Metric,Reference Baseline,Patient ($effectiveCaseId)")
            sb.appendLine("\"Nuclei Count / Density\",\"180 cells / 98.5 mm⁻²\",\"$nucCount cells / ${String.format("%.1f", nucCount * 0.076)} mm⁻²\"")
            sb.appendLine("\"Mean Nuclear Area / Circularity\",\"38.6 px² / 0.86\",\"${String.format("%.1f", nucArea)} px² / ${String.format("%.2f", nucCirc)}\"")
            sb.appendLine("\"Gland Count / Density\",\"16 glands / 12.8 mm⁻²\",\"$glandCount glands / ${String.format("%.1f", glandCount * 0.076)} mm⁻²\"")
            sb.appendLine("\"Mean Gland Area / Irregularity\",\"3,420 px² / 0.31\",\"${String.format("%.0f", glandArea)} px² / ${String.format("%.2f", 1.0 - glandCirc)}\"")
            
            val file = File(context.getExternalFilesDir(null), "colonpath_analysis_table_${effectiveCaseId}.csv")
            file.writeText(sb.toString())
            shareFile(context, file, "text/csv")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generatePdfReport(context: Context): Boolean {
        val file = PdfReportGenerator.generateCaseReportPdf(
            context = context,
            caseResult = caseResult ?: com.example.colonpath_ai.network.CaseResultDto(
                case_id = effectiveCaseId,
                timestamp = "31 Aug 2026",
                status = "COMPLETED"
            ),
            specimenBitmap = overlayBitmap ?: ColonPathRepository.selectedBitmap
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
                title = {
                    Text(
                        "AI-Assisted Report",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Report Header Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Navy800
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
                            text = "Generated on 31 Aug 2026 • Illustrative Demo Findings",
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
                    badge = "PENDING"
                ) {
                    ReportDetailRow("Case ID", effectiveCaseId)
                    ReportDetailRow("Patient ID", "PT-2026-0847")
                    ReportDetailRow("Patient Name", "Sample Patient")
                    ReportDetailRow("Tissue Origin", "Colorectal")
                    ReportDetailRow("Specimen Type", "Biopsy")
                    ReportDetailRow("Staining Protocol", "H&E")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Clinical Notes: Demo data loaded for testing purposes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // 2. Image Information
            item {
                ReportSectionCard(title = "Image Information") {
                    ReportDetailRow("Image Identifier", "IMG-001")
                    ReportDetailRow("Dimensions", "1080 × 1068 px")
                    ReportDetailRow("Acquisition Source", "55c052d5-90ec-46ae-a5f3-34e8778a6fb5-1_all_104801")
                    ReportDetailRow("Calibration", "Calibration unavailable")
                }
            }

            // 3. Image Quality Assessment
            item {
                ReportSectionCard(title = "Image Quality Assessment", badge = "GOOD") {
                    ReportDetailRow("Quality Status", "GOOD")
                    ReportDetailRow("Resolution", quality?.resolution ?: "2048 × 1536")
                    ReportDetailRow("Blur Status", "Acceptable")
                    ReportDetailRow("Optical Calibration", "Calibration unavailable")
                    ReportDetailRow("Quality Accepted", "Yes (Pass)")
                }
            }

            // 4. Analyzed Specimen & AI Overlay
            item {
                ReportSectionCard(title = "Analyzed Specimen & AI Overlay", badge = "HoVer-Net / U-Net Overlay") {
                    var overlayBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(ColonPathRepository.selectedBitmap) }
                    
                    LaunchedEffect(effectiveCaseId) {
                        val bmp = ColonPathApiClient.fetchVisualizationBitmap(effectiveCaseId, "nuclei")
                        if (bmp != null) overlayBitmap = bmp
                    }

                    if (overlayBitmap != null) {
                        Image(
                            bitmap = overlayBitmap!!.asImageBitmap(),
                            contentDescription = "Analyzed Specimen Overlay",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            color = BackgroundLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Specimen overlay rendered from HoVer-Net", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specimen Overlay: HoVer-Net nuclear boundaries (green), epithelial centroids (red), and inflammatory cells (blue).",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // 5. Nuclear Findings
            item {
                ReportSectionCard(title = "Nuclear Findings", badge = "$nucCount Nuclei") {
                    ReportDetailRow("Nuclei Count", "$nucCount")
                    ReportDetailRow("Nuclear Density", "${String.format("%.1f", nucCount * 0.076)} /mm²")
                    ReportDetailRow("Mean Nuclear Area", "${String.format("%.1f", nucArea)} px²")
                    ReportDetailRow("Nuclear Circularity", "${String.format("%.2f", nucCirc)}")
                    ReportDetailRow("Mean Aspect Ratio", "1.34")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dominant Cell Population: Epithelial (48.9% | 892 cells)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // 6. Gland Findings
            item {
                ReportSectionCard(title = "Gland Findings", badge = "$glandCount Glands") {
                    ReportDetailRow("Total Glands Detected", "$glandCount")
                    ReportDetailRow("Mean Gland Area", "${String.format("%.1f", glandArea)} px²")
                    ReportDetailRow("Mean Gland Perimeter", "${String.format("%.1f", glandPerim)} px")
                    ReportDetailRow("Glandular Spacing", "89.4 px")
                    ReportDetailRow("Boundary Irregularity", "${String.format("%.2f", 1.0 - glandCirc)}")
                    ReportDetailRow("Architectural Crowding", "Moderate")
                    ReportDetailRow("Branching Pattern", "Moderate")
                }
            }

            // 7. Reference Retrieval Summary
            item {
                ReportSectionCard(title = "Reference Retrieval Summary") {
                    ReportDetailRow("Closest Match ID", "REF-021")
                    ReportDetailRow("Retrieval Similarity", "94.2%")
                    ReportDetailRow("Reference Classification", "Adenoma-like morphology")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Similarity scores denote computational feature vector proximity to archived cases, not diagnostic disease likelihood.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // 8. AI-Assisted Interpretation
            item {
                ReportSectionCard(title = "AI-Assisted Interpretation") {
                    val expl = caseResult?.explanation?.text?.takeIf { it.isNotBlank() }
                        ?: "Structured multimodal analysis indicates cellular atypia with elevated nuclear density (${String.format("%.1f", nucCount * 0.076)} /mm²) and moderate glandular distortion. Proliferation index aligns with historical adenomatous profiles."
                    Text(
                        text = expl,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Supporting Evidence:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text("• High structural similarity to REF-021 (Adenoma-like morphology, 94.2%)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("• Increased nuclear-to-cytoplasmic ratio observed in epithelial populations", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("• Glandular crowding with moderate branching distortion", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // 9. Limitations & Model Uncertainty
            item {
                ReportSectionCard(title = "Limitations & Model Uncertainty") {
                    Text("• Research prototype analysis only", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("• Morphological similarity does not constitute a diagnosis", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("• Limited reference set for comparison", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("• Not an approved diagnostic device", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uncertainty Note: Computational confidence may be affected by sampling variations and staining consistency.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // 10. Pathologist Review Required Red Box
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = RedLight,
                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.35f))
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
                                text = "Morphological deviations exceed typical baseline parameters. Comprehensive manual review of all slides is strongly recommended.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 11. Bottom Action Buttons: Download PDF & Download CSV
            item {
                Button(
                    onClick = {
                        val success = generatePdfReport(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Report PDF generated successfully!"
                                else "Failed to generate PDF."
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500)
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
                                if (success) "Analysis Table CSV exported successfully!"
                                else "Failed to export CSV."
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue50,
                        contentColor = Blue500
                    ),
                    border = BorderStroke(1.dp, Blue500.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
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
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
