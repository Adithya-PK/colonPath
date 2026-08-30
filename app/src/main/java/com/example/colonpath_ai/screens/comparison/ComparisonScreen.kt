package com.example.colonpath_ai.screens.comparison

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(onBack: () -> Unit, onReport: () -> Unit) {
    val caseId = SampleDataRepository.activeCaseId ?: "COL-2026-001"
    val analysisResult = SampleDataRepository.getAnalysisForCase(caseId)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reference Comparison") },
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
                Badge(containerColor = AmberLight) {
                    Text("Demo Analysis", color = TextPrimary)
                }
            }

            item {
                SectionHeader("Retrieved Reference Cases")
                analysisResult.referenceComparison.references.forEach { ref ->
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text(ref.referenceId, fontWeight = FontWeight.Bold)
                            Text("${ref.similarityScore}% similarity", color = Blue500)
                            Text(ref.category)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                SectionHeader("Regular / Reference vs Patient")
                ComparisonTable(
                    metrics = analysisResult.referenceComparison.metrics,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                SectionHeader("Why This Result?", expandable = true, expanded = expanded, onToggle = { expanded = !expanded })
                if (expanded) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Supporting Computational Observations", fontWeight = FontWeight.Bold)
                            Text("• Morphological features are consistent with typical reference cases.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reference Comparison Basis", fontWeight = FontWeight.Bold)
                            Text("• Explanation of similarity scoring.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Limitations", fontWeight = FontWeight.Bold)
                            Text("• Similarity ≠ diagnosis, prototype data, requires pathologist review.")
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AmberLight)) {
                    Text(
                        "Similarity scores represent retrieval similarity and are not diagnostic probabilities.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            item {
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                    Text("View AI-Assisted Report")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
