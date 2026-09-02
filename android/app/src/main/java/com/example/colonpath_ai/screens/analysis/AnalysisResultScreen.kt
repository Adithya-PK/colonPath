package com.example.colonpath_ai.screens.analysis

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.network.ColonPathApiClient
import com.example.colonpath_ai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    onBack: () -> Unit,
    onMorphology: () -> Unit,
    onComparison: () -> Unit,
    onReport: () -> Unit
) {
    var selectedVisType by remember { mutableStateOf("original") }
    var currentOverlayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingOverlay by remember { mutableStateOf(false) }

    // Copilot Q&A state
    var showCopilotDialog by remember { mutableStateOf(false) }
    var copilotQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val caseResult = ColonPathRepository.currentCaseResult
    val caseId = caseResult?.case_id ?: ColonPathRepository.activeCaseId ?: "UNKNOWN_CASE"

    // Fetch visualization overlay bitmap when tab changes
    LaunchedEffect(selectedVisType, caseId) {
        if (selectedVisType == "original" && ColonPathRepository.selectedBitmap != null) {
            currentOverlayBitmap = ColonPathRepository.selectedBitmap
        } else {
            isLoadingOverlay = true
            val bmp = ColonPathApiClient.fetchVisualizationBitmap(caseId, selectedVisType)
            currentOverlayBitmap = bmp
            isLoadingOverlay = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Case Analysis Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCopilotDialog = true }) {
                        Icon(Icons.Outlined.Psychology, contentDescription = "Ask Copilot", tint = Blue500)
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
            // 1. Case Metadata Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Case ID: $caseId", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (caseResult?.prediction?.`class` == "TUM") AmberLight.copy(alpha = 0.2f) else Blue50
                            ) {
                                Text(
                                    text = caseResult?.status?.uppercase() ?: "COMPLETED",
                                    color = if (caseResult?.prediction?.`class` == "TUM") Amber500 else Blue500,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Timestamp: ${caseResult?.timestamp ?: "Just now"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // 2. AI Multimodal Prediction Card
            item {
                val predClass = caseResult?.prediction?.`class` ?: "UNKNOWN"
                val conf = (caseResult?.prediction?.calibrated_confidence ?: caseResult?.prediction?.confidence ?: 0.0) * 100.0
                val tumorProb = (caseResult?.prediction?.tumor_probability ?: 0.0) * 100.0
                val isTumor = predClass == "TUM" || tumorProb >= 50.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTumor) AmberLight.copy(alpha = 0.15f) else Blue50
                    ),
                    border = BorderStroke(1.dp, if (isTumor) Amber500.copy(alpha = 0.4f) else Blue100)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("AI Predicted Class", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                Text(
                                    text = when (predClass) {
                                        "TUM" -> "Colorectal Adenocarcinoma (TUM)"
                                        "NORM" -> "Normal Mucosa (NORM)"
                                        "STR" -> "Stroma (STR)"
                                        "LYM" -> "Lymphocytes (LYM)"
                                        "MUC" -> "Mucus (MUC)"
                                        "DEB" -> "Debris / Necrosis (DEB)"
                                        else -> predClass
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTumor) Amber500 else Navy800
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard("Calibrated Confidence", "${String.format("%.1f", conf)}%", modifier = Modifier.weight(1f))
                            MetricCard("Tumor Likelihood", "${String.format("%.1f", tumorProb)}%", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 3. Interactive Multi-Layer Visualization Viewer
            item {
                SectionHeader(
                    title = "Specimen & Computational Overlays",
                    subtitle = "Streamed directly from verified case artifact endpoints"
                )
                Spacer(modifier = Modifier.height(6.dp))

                val visModes = listOf(
                    "original" to "Original",
                    "glands" to "Glands",
                    "nuclei" to "Nuclei",
                    "regions" to "Regions",
                    "uncertainty" to "Uncertainty"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    ScrollableTabRow(
                        selectedTabIndex = visModes.indexOfFirst { it.first == selectedVisType }.coerceAtLeast(0),
                        containerColor = SurfaceWhite,
                        contentColor = Blue500,
                        edgePadding = 0.dp,
                        divider = {},
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    ) {
                        visModes.forEach { (typeKey, typeLabel) ->
                            val isSelected = selectedVisType == typeKey
                            Tab(
                                selected = isSelected,
                                onClick = { selectedVisType = typeKey },
                                text = {
                                    Text(
                                        text = typeLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Blue500 else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingOverlay) {
                                CircularProgressIndicator(color = Blue500, modifier = Modifier.size(36.dp))
                            } else if (currentOverlayBitmap != null) {
                                Image(
                                    bitmap = currentOverlayBitmap!!.asImageBitmap(),
                                    contentDescription = "Visualization: $selectedVisType",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text("Overlay not available on disk", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 4. Uncertainty, Calibration & OOD Status
            item {
                SectionHeader("Model Uncertainty & Distribution")
                val unc = caseResult?.uncertainty
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Uncertainty Level", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(unc?.level ?: "LOW", fontWeight = FontWeight.Bold, color = if (unc?.level == "HIGH") RedError else Blue500)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Shannon Entropy", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${String.format("%.4f", unc?.entropy ?: 0.0)}", fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("OOD Status", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(unc?.ood_status ?: "IN_DISTRIBUTION", fontWeight = FontWeight.Bold, color = if (unc?.is_ood == true) RedError else GreenSuccess)
                        }
                    }
                }
            }

            // 5. Multi-Source Consensus Agreement
            item {
                SectionHeader("Multi-Source Model Consensus")
                val agr = caseResult?.model_agreement
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Consensus Level", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(agr?.level ?: "HIGH", fontWeight = FontWeight.Bold, color = if (agr?.level == "LOW") RedError else Blue500)
                        }
                        if (!agr?.summary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(agr?.summary ?: "", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }

            // 6. Nuclear & Gland Morphometry Evidence
            item {
                SectionHeader("Morphometry Evidence")
                val nuc = caseResult?.nuclear_evidence
                val gland = caseResult?.gland_evidence

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Total Nuclei", "${nuc?.total_count ?: 0}", modifier = Modifier.weight(1f))
                    MetricCard("Mean Nuc Area", "${String.format("%.1f", nuc?.mean_area_px2 ?: 0.0)}", "px²", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Total Glands", "${gland?.total_count ?: 0}", modifier = Modifier.weight(1f))
                    MetricCard("Gland Circularity", "${String.format("%.2f", gland?.mean_circularity ?: 0.0)}", modifier = Modifier.weight(1f))
                }
            }

            // 7. Reference Cohort Comparison
            item {
                SectionHeader("Reference Cohort Match")
                val ref = caseResult?.reference_comparison
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Top Reference Match", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                "${ref?.top_category?.uppercase() ?: "NORMAL"} (${String.format("%.1f", ref?.top_similarity_percent ?: 0.0)}%)",
                                fontWeight = FontWeight.Bold,
                                color = Blue500
                            )
                        }
                        if (!ref?.insight.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ref?.insight ?: "", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }

            // 8. Medical Research Disclaimer
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Blue50,
                    border = BorderStroke(1.dp, Blue100)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Blue500, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Decision-support output for research use; requires qualified pathologist review.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Navy800
                        )
                    }
                }
            }

            // 9. Interactive Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Action: Full Diagnostic Report (PDF)
                    Button(
                        onClick = onReport,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View AI Diagnostic Report & PDF")
                    }

                    // Secondary Actions: Detailed Morphometry & Cohort Comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMorphology,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Blue500.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.Biotech, contentDescription = null, modifier = Modifier.size(16.dp), tint = Blue500)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Morphometry", color = Blue500)
                        }

                        OutlinedButton(
                            onClick = onComparison,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Blue500.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp), tint = Blue500)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Comparison", color = Blue500)
                        }
                    }

                    // Copilot Inquiry Action
                    Button(
                        onClick = { showCopilotDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        Icon(Icons.Outlined.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask Pathologist Copilot (MedGemma)")
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Pathologist Copilot Q&A Modal Dialog
        if (showCopilotDialog) {
            AlertDialog(
                onDismissRequest = { showCopilotDialog = false },
                title = { Text("Pathologist Copilot Q&A", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                        Text(
                            "Inquire regarding cytopathology, architectural distortion, invasion criteria, or priority regions:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = copilotQuery,
                            onValueChange = { copilotQuery = it },
                            placeholder = { Text("e.g. Why was Region R_01 prioritized?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (ColonPathRepository.isCopilotLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Blue500, modifier = Modifier.size(28.dp))
                            }
                        }

                        val lastAnswer = ColonPathRepository.copilotHistory.lastOrNull()
                        if (lastAnswer != null && !ColonPathRepository.isCopilotLoading) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundLight,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Q: ${lastAnswer.question}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Blue500)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(lastAnswer.answer, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Model: ${lastAnswer.model} • Validated: ${lastAnswer.validated}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (copilotQuery.isNotBlank()) {
                                coroutineScope.launch {
                                    ColonPathRepository.askCopilot(copilotQuery)
                                    copilotQuery = ""
                                }
                            }
                        },
                        enabled = copilotQuery.isNotBlank() && !ColonPathRepository.isCopilotLoading
                    ) {
                        Text("Ask")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCopilotDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
