package com.example.colonpath_ai.screens.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.ui.theme.AmberLight
import com.example.colonpath_ai.ui.theme.AmberWarning
import com.example.colonpath_ai.ui.theme.BackgroundLight
import com.example.colonpath_ai.ui.theme.Blue500
import com.example.colonpath_ai.ui.theme.CardBorder
import com.example.colonpath_ai.ui.theme.GreenSuccess
import com.example.colonpath_ai.ui.theme.TextPrimary
import com.example.colonpath_ai.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AnalysisProgressScreen(
    caseId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val stages = listOf(
        "Image received",
        "Image quality check",
        "Nuclear analysis",
        "Nuclear classification",
        "Gland segmentation",
        "Morphology analysis",
        "Reference retrieval",
        "AI reasoning",
        "Report generation"
    )

    var currentStage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in stages.indices) {
            currentStage = i
            delay((180..280).random().toLong())
        }
        currentStage = stages.size
        delay(200)
        onComplete()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Analyzing H&E Sample",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = caseId,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AmberLight
            ) {
                Text(
                    text = "Prototype Simulation",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberWarning,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            val progress = currentStage.toFloat() / stages.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Blue500,
                trackColor = CardBorder,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stages.forEachIndexed { index, stage ->
                    AnimatedVisibility(
                        visible = index <= currentStage,
                        enter = fadeIn(animationSpec = tween(180)) + slideInVertically(animationSpec = tween(180))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when {
                                index < currentStage -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = stage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
                                index == currentStage -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Blue500,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = stage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Blue500,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                else -> {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        border = BorderStroke(2.dp, CardBorder)
                                    ) {}
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = stage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
