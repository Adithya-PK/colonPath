package com.example.colonpath_ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.ui.theme.*

@Composable
fun ImageViewer(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("Original", "Nuclear", "Gland", "Combined")
    
    Column(modifier = modifier.fillMaxWidth()) {
        ScrollableTabRow(
            selectedTabIndex = modes.indexOf(selectedMode).takeIf { it >= 0 } ?: 0,
            containerColor = BackgroundLight,
            contentColor = Blue500,
            edgePadding = 0.dp
        ) {
            modes.forEach { mode ->
                Tab(
                    selected = selectedMode == mode,
                    onClick = { onModeChange(mode) },
                    text = { Text(mode) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HEPink),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "H&E",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = HEPinkDark
                )
                Text(
                    text = selectedMode,
                    style = MaterialTheme.typography.titleMedium,
                    color = HEPinkDark
                )
                if (selectedMode != "Original") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Navy800.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Prototype Overlay: $selectedMode",
                            color = SurfaceWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
