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

            // Stage 0 -> 1 -> 2 in first 2 seconds, then stays at Stage 2 (Nuclear Segmentation) during live computation
            val stageAdvancer = launch {
                delay(800)
                currentStage = 1
                delay(1000)
                currentStage = 2
            }

            val tickerJob = launch {
                while (isActive) {
                    delay(1000)
                    elapsedSeconds++
                }
            }

            val result = com.example.colonpath_ai.data.ColonPathRepository.executeAnalysis(caseId)
            stageAdvancer.cancel()
            tickerJob.cancel()

            if (result.isSuccess) {
                // Rapidly sweep through remaining stages (3..8) to 100% on success
                for (i in (currentStage + 1)..stages.size) {
                    currentStage = i
                    delay(60)
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
                    text = "⏱️ Deep neural inference in progress • ${elapsedSeconds}s elapsed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blue500,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Analysis Pipeline Progress",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Blue500
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Blue500,
                        trackColor = Blue50
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stages.size) { index ->
                    val stage = stages[index]
                    val isPassed = index < currentStage
                    val isCurrent = index == currentStage

                    val backgroundColor by animateColorAsState(
                        targetValue = when {
                            isPassed -> GreenSuccess.copy(alpha = 0.05f)
                            isCurrent -> Blue50
                            else -> SurfaceWhite
                        },
                        animationSpec = tween(300),
                        label = "cardBg"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = when {
                            isPassed -> GreenSuccess.copy(alpha = 0.3f)
                            isCurrent -> Blue500.copy(alpha = 0.4f)
                            else -> CardBorder
                        },
                        animationSpec = tween(300),
                        label = "cardBorder"
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = backgroundColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Number / Check Circle
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .then(if (isCurrent) Modifier.scale(pulseScale) else Modifier)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPassed -> GreenSuccess
                                            isCurrent -> Blue500
                                            else -> CardBorder
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (isCurrent) Color.White else TextTertiary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stage.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCurrent) Blue500 else TextPrimary
                                )
                                Text(
                                    text = stage.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Error Display if any
            if (executionError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = RedError.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Analysis Error: $executionError",
                            color = RedError,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = RedError),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}
