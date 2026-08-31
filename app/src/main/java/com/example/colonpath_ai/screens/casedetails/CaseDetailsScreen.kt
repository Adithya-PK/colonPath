package com.example.colonpath_ai.screens.casedetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.model.CaseStatus
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailsScreen(
    caseId: String,
    onBack: () -> Unit,
    onViewReport: () -> Unit,
    onRetake: () -> Unit = {}
) {
    val case = SampleDataRepository.getCaseById(caseId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (case == null) {
        ErrorState(
            title = "Case Not Found",
            message = "The requested case could not be loaded or was deleted.",
            actionLabel = "Go Back",
            onAction = onBack,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    var expandedSections by remember { mutableStateOf(setOf("Case Header", "Status & Actions", "Patient Information")) }
    fun toggleSection(section: String) {
        val newSet = expandedSections.toMutableSet()
        if (newSet.contains(section)) newSet.remove(section) else newSet.add(section)
        expandedSections = newSet
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Case") },
            text = { Text("Are you sure you want to delete case ${case.caseId}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        SampleDataRepository.deleteCase(case.caseId)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Case Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Case",
                            tint = RedError
                        )
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
            // If failed, show prominent action banner
            if (case.status == CaseStatus.FAILED) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RedLight),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedError.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = RedError)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analysis Quality Alert",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RedError
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Image quality or tissue contrast fell below diagnostic thresholds. You can retake the sample or request manual pathologist review.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onRetake,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                                ) {
                                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retake Image")
                                }
                                OutlinedButton(
                                    onClick = {
                                        SampleDataRepository.updateCaseStatus(case.caseId, CaseStatus.PENDING_REVIEW)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Outlined.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Request Review")
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Case Details
            item {
                SectionHeader(
                    title = "Case Details",
                    expandable = true,
                    expanded = expandedSections.contains("Case Header"),
                    onToggle = { toggleSection("Case Header") }
                )
                if (expandedSections.contains("Case Header")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = case.caseId,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                StatusBadge(case.status)
                            }
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Analysis Date", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.analysisDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tissue Origin", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.tissue, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sample Type", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.sampleType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Staining Protocol", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.stain, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Section 2: Update Status
            item {
                SectionHeader(
                    title = "Update Status",
                    expandable = true,
                    expanded = expandedSections.contains("Status & Actions"),
                    onToggle = { toggleSection("Status & Actions") }
                )
                if (expandedSections.contains("Status & Actions")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            Text("Mark Case Status:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val statuses = listOf(
                                    CaseStatus.COMPLETED to "Completed",
                                    CaseStatus.PENDING_REVIEW to "Review",
                                    CaseStatus.IN_PROGRESS to "In Progress",
                                    CaseStatus.FAILED to "Failed"
                                )
                                statuses.forEach { (st, label) ->
                                    FilterChip(
                                        selected = case.status == st,
                                        onClick = {
                                            SampleDataRepository.updateCaseStatus(case.caseId, st)
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Patient Information (Matches exact same width, borders, padding and corner radius)
            item {
                SectionHeader(
                    title = "Patient Information",
                    expandable = true,
                    expanded = expandedSections.contains("Patient Information"),
                    onToggle = { toggleSection("Patient Information") }
                )
                if (expandedSections.contains("Patient Information")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Patient ID", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.patient.patientId, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Patient Name", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text(case.patient.patientName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                            }
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                            val displayNotes = case.notes.ifBlank { case.patient.notes }.ifBlank { "None recorded" }
                            Text("Clinical Notes:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(displayNotes, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }

            // Section 4: Action Button
            item {
                Button(
                    onClick = onViewReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Report")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
