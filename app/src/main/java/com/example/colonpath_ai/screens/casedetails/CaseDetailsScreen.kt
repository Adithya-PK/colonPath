package com.example.colonpath_ai.screens.casedetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.filled.Warning
import com.example.colonpath_ai.model.CaseStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailsScreen(
    caseId: String,
    onBack: () -> Unit,
    onViewReport: () -> Unit,
    onRetake: () -> Unit = {}
) {
    val case = SampleDataRepository.getCaseById(caseId)
    val analysis = SampleDataRepository.getAnalysisForCase(caseId)
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // If failed, show prominent action banner
            if (case.status == CaseStatus.FAILED) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RedLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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

            item {
                SectionHeader(
                    title = "Case Details",
                    expandable = true,
                    expanded = expandedSections.contains("Case Header"),
                    onToggle = { toggleSection("Case Header") }
                )
                if (expandedSections.contains("Case Header")) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(case.caseId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                StatusBadge(case.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Date: ${case.analysisDate}")
                            Text("Tissue: ${case.tissue}")
                            Text("Sample Type: ${case.sampleType}")
                            Text("Stain: ${case.stain}")
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Update Status",
                    expandable = true,
                    expanded = expandedSections.contains("Status & Actions"),
                    onToggle = { toggleSection("Status & Actions") }
                )
                if (expandedSections.contains("Status & Actions")) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mark Case Status:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
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

            item {
                SectionHeader(
                    title = "Patient Information",
                    expandable = true,
                    expanded = expandedSections.contains("Patient Information"),
                    onToggle = { toggleSection("Patient Information") }
                )
                if (expandedSections.contains("Patient Information")) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Patient ID: ${case.patient.patientId}")
                            Text("Name: ${case.patient.patientName}")
                            Spacer(modifier = Modifier.height(6.dp))
                            val displayNotes = case.notes.ifBlank { case.patient.notes }.ifBlank { "None recorded" }
                            Text("Notes: $displayNotes", color = TextPrimary)
                        }
                    }
                }
            }

            item {
                Button(onClick = onViewReport, modifier = Modifier.fillMaxWidth()) {
                    Text("View Full Report")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
