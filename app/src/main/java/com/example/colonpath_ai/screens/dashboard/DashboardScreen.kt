package com.example.colonpath_ai.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.CaseCard
import com.example.colonpath_ai.components.PipelineStepItem
import com.example.colonpath_ai.components.SectionHeader
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.AmberLight
import com.example.colonpath_ai.ui.theme.BackgroundLight
import com.example.colonpath_ai.ui.theme.Blue50
import com.example.colonpath_ai.ui.theme.Blue500
import com.example.colonpath_ai.ui.theme.CardBorder
import com.example.colonpath_ai.ui.theme.Navy600
import com.example.colonpath_ai.ui.theme.SurfaceWhite
import com.example.colonpath_ai.ui.theme.TextPrimary
import com.example.colonpath_ai.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.ButtonDefaults
import com.example.colonpath_ai.model.CaseStatus

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.colonpath_ai.R

@Composable
fun DashboardScreen(
    onNewAnalysis: () -> Unit,
    onLiveAnalysis: () -> Unit,
    onHistory: () -> Unit,
    onCaseClick: (String) -> Unit
) {
    val cases = SampleDataRepository.sampleCaseHistory
    val totalCount = remember(cases.size) { cases.size.toString() }
    val completedCount = remember(cases.toList()) { cases.count { it.status == CaseStatus.COMPLETED }.toString() }
    val pendingCount = remember(cases.toList()) { cases.count { it.status == CaseStatus.PENDING_REVIEW || it.status == CaseStatus.IN_PROGRESS }.toString() }
    val latestDate = remember(cases.firstOrNull()) { cases.firstOrNull()?.analysisDate ?: "None" }
    val recentCase = cases.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.colonpath_logo),
                        contentDescription = "ColonPath-AI Official Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                Column {
                    Text(
                        text = "ColonPath-AI",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "AI-Assisted Colorectal Histopathology Analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Blue50
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Colorectal Tissue Analysis",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Analyze H&E histopathology images using computer vision, quantitative morphology and AI-assisted interpretation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onNewAnalysis,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue500,
                                contentColor = SurfaceWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Analysis")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onLiveAnalysis,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.colonpath_ai.ui.theme.Blue100,
                                contentColor = com.example.colonpath_ai.ui.theme.Navy800
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Analysis")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Overview",
                expandable = false,
                expanded = true,
                onToggle = {},
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Folder,
                        value = totalCount,
                        label = "Total Cases"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CheckCircle,
                        value = completedCount,
                        label = "Completed"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Schedule,
                        value = pendingCount,
                        label = "Pending Review"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CalendarToday,
                        value = latestDate,
                        label = "Latest Analysis"
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent Case",
                expandable = false,
                expanded = true,
                onToggle = {}
            )
            if (recentCase != null) {
                CaseCard(
                    case = recentCase,
                    onClick = { onCaseClick(recentCase.caseId) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "No cases recorded yet. Tap 'New Analysis' to start.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Analysis Pipeline",
                expandable = false,
                expanded = true,
                onToggle = {}
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PipelineStepItem(
                    number = "01",
                    title = "Image Quality",
                    description = "Assess focus, contrast, staining and tissue coverage.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
                PipelineStepItem(
                    number = "02",
                    title = "Nuclear Analysis",
                    description = "Detect and analyze nuclear structures.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
                PipelineStepItem(
                    number = "03",
                    title = "Gland Analysis",
                    description = "Segment and characterize glandular architecture.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
                PipelineStepItem(
                    number = "04",
                    title = "Morphology",
                    description = "Calculate quantitative tissue characteristics.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
                PipelineStepItem(
                    number = "05",
                    title = "Reference Comparison",
                    description = "Compare with reference tissue patterns.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
                PipelineStepItem(
                    number = "06",
                    title = "AI Report",
                    description = "Present structured computational observations.",
                    isCompleted = false,
                    isActive = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = AmberLight
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = TextPrimary
                    )
                    Column {
                        Text(
                            text = "Prototype / Research Use",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This application is a prototype for demonstration purposes only. It is not intended for clinical use or diagnostic decision making. Always consult a qualified pathologist.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Blue500,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
