package com.example.colonpath_ai.screens.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

import com.example.colonpath_ai.util.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(caseId: String? = null, onBack: () -> Unit) {
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
            val sb = java.lang.StringBuilder()
            sb.appendLine("Metric,Reference,Patient")
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Navy800)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("COLONPATH-AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                        Text("AI-Assisted Histopathology Analysis", style = MaterialTheme.typography.bodyMedium, color = SurfaceWhite)
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge(containerColor = AmberLight) {
                            Text("Prototype Report", color = TextPrimary)
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Patient / Case Information", fontWeight = FontWeight.Bold)
                        Text("Case ID: ${analysisResult.case.caseId} | Patient ID: ${analysisResult.case.patient.patientId}")
                        Text("Patient: ${analysisResult.case.patient.patientName} | Tissue: ${analysisResult.case.tissue}")
                        Text("Stain: ${analysisResult.case.stain} | Date: ${analysisResult.case.analysisDate}")
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Information", fontWeight = FontWeight.Bold)
                        Text("Image ID: ${analysisResult.imageInfo.imageId} | Dimensions: ${analysisResult.imageInfo.width} × ${analysisResult.imageInfo.height}")
                        Text("Source: ${analysisResult.imageInfo.source} | Calibration: ${analysisResult.imageInfo.calibrationStatus}")
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Quality", fontWeight = FontWeight.Bold)
                        Text("Status: ${analysisResult.imageQuality.status} | Resolution: ${analysisResult.imageQuality.resolution}")
                        Text("Blur: ${analysisResult.imageQuality.blurStatus} | Accepted: ${if (analysisResult.imageQuality.accepted) "Yes" else "No"}")
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nuclear Findings", fontWeight = FontWeight.Bold)
                        Text("${analysisResult.nuclearAnalysis.nucleiDetected} nuclei detected, nuclear density ${analysisResult.nuclearAnalysis.nuclearDensity}/mm², mean area ${analysisResult.nuclearAnalysis.meanNuclearArea} px², circularity ${analysisResult.nuclearAnalysis.nuclearCircularity}")
                        Text("Classification summary: Predominant epithelial population (${analysisResult.nuclearClassification.categories.firstOrNull()?.percentage ?: 0.0}%)")
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gland Findings", fontWeight = FontWeight.Bold)
                        Text("${analysisResult.glandAnalysis.glandCount} glands detected, mean area ${analysisResult.glandAnalysis.meanGlandArea} px²")
                        Text("Architectural irregularity: ${analysisResult.glandAnalysis.boundaryIrregularity}, crowding: ${analysisResult.glandAnalysis.crowding}")
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Reference Comparison", fontWeight = FontWeight.Bold)
                        val topMatch = analysisResult.referenceComparison.references.firstOrNull()
                        Text("Top match: ${topMatch?.referenceId} (${topMatch?.similarityScore}% similarity, ${topMatch?.category})")
                        Text("Similarity represents retrieval similarity, not diagnostic probability.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI-Assisted Interpretation", fontWeight = FontWeight.Bold)
                        Text(analysisResult.aiReport.interpretation)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Supporting Evidence:", fontWeight = FontWeight.Bold)
                        analysisResult.aiReport.supportingEvidence.forEach {
                            Text("• $it")
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Limitations & Uncertainty", fontWeight = FontWeight.Bold)
                        analysisResult.aiReport.limitations.items.forEach {
                            Text("• $it")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(analysisResult.aiReport.uncertainty, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = RedLight)) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = RedError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("PATHOLOGIST REVIEW REQUIRED", fontWeight = FontWeight.Bold, color = RedError)
                            Text(analysisResult.aiReport.pathologistReview.message)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val success = generatePdfReport(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(if (success) "Report PDF generated successfully! Opened share/view sheet." else "Failed to generate PDF.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Report")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val success = exportAnalysisTableCsv(context)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(if (success) "CSV exported successfully! Opened share/view sheet." else "Failed to export CSV.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Analysis Table")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
