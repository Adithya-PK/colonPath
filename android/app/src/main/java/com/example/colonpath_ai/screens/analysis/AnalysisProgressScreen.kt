package com.example.colonpath_ai.screens.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colonpath_ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

data class AnalysisStage(
    val title: String,
    val description: String
)

@Composable
fun AnalysisProgressScreen(
    caseId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val stages = remember {
        listOf(
            AnalysisStage("Image Ingestion & Standardization", "Validating resolution, pixel format, and scale calibration"),
            AnalysisStage("Optical Quality Assessment", "Checking blur metric, contrast depth, and staining clarity"),
            AnalysisStage("Nuclear Segmentation", "Detecting individual epithelial and stromal nuclei boundaries"),
            AnalysisStage("Nuclear Pleomorphism Classification", "Quantifying nuclear area, circularity, and density"),
            AnalysisStage("Glandular Architecture Segmentation", "Extracting epithelial lumen contours and irregularity index"),
            AnalysisStage("Morphological Feature Extraction", "Calculating spatial crowding, perimeter, and branching"),
            AnalysisStage("Reference Database Vector Retrieval", "Querying histological embeddings against reference cases"),
            AnalysisStage("Multi-Evidence AI Reasoning", "Synthesizing computational observations and uncertainty bounds"),
            AnalysisStage("Diagnostic Report Synthesis", "Finalizing structured diagnostic summary and morphology tables")
        )
    }

    var currentStage by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Smooth animated progress value
    val targetProgress = (currentStage.toFloat() / stages.size.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Pulse animation for active step badge
    val infiniteTransition = rememberInfiniteTransition(label = "stepPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var isExecuting by remember { mutableStateOf(false) }
    var executionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!isExecuting) {
            isExecuting = true
            executionError = null
            currentStage = 0
            elapsedSeconds = 0

            val tickerJob = launch {
                while (isActive) {
                    delay(1000)
                    elapsedSeconds++
                    if (currentStage < 7 && elapsedSeconds % 4 == 0) {
                        currentStage = (currentStage + 1).coerceAtMost(7)
                    }
                }
            }

            val result = com.example.colonpath_ai.data.ColonPathRepository.executeAnalysis(caseId)
            tickerJob.cancel()

            if (result.isSuccess) {
                for (i in (currentStage + 1)..stages.size) {
                    currentStage = i
                    delay(70)
                }
                delay(120)
                onComplete()
            } else {
                executionError = result.exceptionOrNull()?.message ?: "Analysis execution error"
            }
            isExecuting = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Blue50,
                    border = BorderStroke(1.dp, Blue500.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Science,
                            contentDescription = null,
                            tint = Blue500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Histopathology Pipeline • $caseId",
                            style = MaterialTheme.typography.labelMedium,
                            color = Blue500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Analyzing Specimen",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = if (isExecuting) "Deep neural inference in progress • ${elapsedSeconds}s elapsed" else "Executing neural morphology extraction and reference matching",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Overall Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentStage < stages.size) "Step ${currentStage + 1} of ${stages.size}" else "Pipeline Complete",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Blue500
                        )
                    }

                    // Polished Gradient Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(BackgroundLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedProgress)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Blue500, Navy800)
                                    )
                                )
                        )
                    }

                    val activeDesc = if (currentStage < stages.size) stages[currentStage].description else "Compiling results..."
                    Text(
                        text = activeDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Stages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stages.size) { index ->
                    val stage = stages[index]
                    val isDone = index < currentStage
                    val isCurrent = index == currentStage

                    val cardBg by animateColorAsState(
                        targetValue = when {
                            isCurrent -> Blue50.copy(alpha = 0.5f)
                            isDone -> SurfaceWhite
                            else -> SurfaceWhite.copy(alpha = 0.6f)
                        },
                        label = "cardBg"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> Blue500.copy(alpha = 0.4f)
                            isDone -> GreenSuccess.copy(alpha = 0.3f)
                            else -> CardBorder.copy(alpha = 0.4f)
                        },
                        label = "borderBg"
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Indicator Icon / Loader
                            Box(
                                modifier = Modifier.size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isDone -> {
                                        Surface(
                                            shape = CircleShape,
                                            color = GreenSuccess.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = GreenSuccess,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    isCurrent -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .scale(pulseScale),
                                            color = Blue500,
                                            strokeWidth = 2.5.dp,
                                            trackColor = Blue50
                                        )
                                    }
                                    else -> {
                                        Surface(
                                            shape = CircleShape,
                                            color = BackgroundLight,
                                            border = BorderStroke(1.dp, CardBorder),
                                            modifier = Modifier.size(18.dp)
                                        ) {}
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stage.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = when {
                                        isCurrent -> Blue500
                                        isDone -> TextPrimary
                                        else -> TextSecondary.copy(alpha = 0.6f)
                                    }
                                )
                                if (isCurrent) {
                                    Text(
                                        text = stage.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (executionError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AmberLight.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Analysis Execution Error", color = RedError, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(executionError ?: "Unknown error", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Cancel / Back Action Button
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (executionError != null) "Go Back" else "Cancel Analysis", color = TextSecondary)
            }
        }
    }
}
