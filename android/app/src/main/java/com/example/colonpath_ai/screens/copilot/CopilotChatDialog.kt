package com.example.colonpath_ai.screens.copilot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CopilotChatDialog(
    caseId: String,
    onDismiss: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val quickQuestions = listOf(
        "Summarize active case findings",
        "Why was pathologist review recommended?",
        "Explain nuclear pleomorphism & density",
        "What is the glandular architecture integrity?",
        "Explain why Region R_01 was prioritized"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Blue50,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Psychology, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pathologist Copilot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Grounded MedGemma 1.5 4B IT • Case $caseId",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextSecondary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder.copy(alpha = 0.5f))

                // Quick Prompt Chips
                Text("Suggested Inquiries:", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickQuestions) { q ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Blue50,
                            border = BorderStroke(0.8.dp, Blue500.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                queryText = q
                                coroutineScope.launch {
                                    ColonPathRepository.askCopilot(q)
                                    queryText = ""
                                }
                            }
                        ) {
                            Text(
                                text = q,
                                style = MaterialTheme.typography.labelSmall,
                                color = Blue500,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ColonPathRepository.copilotHistory.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = BackgroundLight,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = Blue500, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Pathology Assistant Ready", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Text(
                                        "Ask any clinical inquiry regarding cell pleomorphism, gland distortion, probability calibration, or region prioritization. All answers are deterministically verified against active case CV tensors.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    items(ColonPathRepository.copilotHistory) { item ->
                        // User Query Bubble
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp),
                                color = Blue500
                            ) {
                                Text(
                                    text = item.question,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SurfaceWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Copilot Grounded Answer Bubble
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp),
                                color = BackgroundLight,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Grounded Clinical Answer (${item.model})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy800
                                        )
                                    }
                                    Text(
                                        text = item.answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                    if (item.selected_region_id != null) {
                                        Text(
                                            text = "Focused Region: ${item.selected_region_id}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Blue500,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (ColonPathRepository.isCopilotLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = Blue500, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Grounded evidence synthesis via MedGemma...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("Ask clinical inquiry...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = CardBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                val q = queryText
                                queryText = ""
                                coroutineScope.launch {
                                    ColonPathRepository.askCopilot(q)
                                }
                            }
                        },
                        enabled = queryText.isNotBlank() && !ColonPathRepository.isCopilotLoading
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (queryText.isNotBlank()) Blue500 else CardBorder,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = SurfaceWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
