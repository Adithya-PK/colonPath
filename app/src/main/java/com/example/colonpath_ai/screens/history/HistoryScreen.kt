package com.example.colonpath_ai.screens.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.components.*
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

import androidx.compose.material.icons.outlined.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onCaseClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var caseToDelete by remember { mutableStateOf<com.example.colonpath_ai.model.Case?>(null) }
    val filters = listOf("All", "Completed", "Pending Review", "In Progress", "Failed")

    if (caseToDelete != null) {
        AlertDialog(
            onDismissRequest = { caseToDelete = null },
            title = { Text("Delete Case") },
            text = { Text("Are you sure you want to delete ${caseToDelete?.caseId}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        caseToDelete?.let { SampleDataRepository.deleteCase(it.caseId) }
                        caseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { caseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Case History", style = MaterialTheme.typography.displaySmall)
            Text("Previous analyses and cases", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search cases or patients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        val allCases = SampleDataRepository.sampleCaseHistory
        val filteredCases = remember(searchQuery, selectedFilter, allCases.toList()) {
            allCases.filter { case ->
                (selectedFilter == "All" || case.status.name.replace("_", " ").equals(selectedFilter, ignoreCase = true)) &&
                (searchQuery.isEmpty() || case.caseId.contains(searchQuery, ignoreCase = true) || case.patient.patientName.contains(searchQuery, ignoreCase = true) || case.patient.patientId.contains(searchQuery, ignoreCase = true))
            }
        }

        if (filteredCases.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Info,
                title = "No Cases Found",
                message = "Try a different search or filter.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCases, key = { it.caseId }) { case ->
                    CaseCard(
                        case = case,
                        onClick = { onCaseClick(case.caseId) },
                        onDelete = { caseToDelete = case }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
