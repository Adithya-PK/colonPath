package com.example.colonpath_ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.R
import com.example.colonpath_ai.data.ColonPathRepository
import com.example.colonpath_ai.ui.theme.*

@Composable
fun ImageViewer(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("Original", "Nuclear", "Gland", "Overlay")

    Column(modifier = modifier.fillMaxWidth()) {
        ScrollableTabRow(
            selectedTabIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
            containerColor = SurfaceWhite,
            contentColor = Blue500,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            modes.forEach { mode ->
                val isSelected = selectedMode == mode
                Tab(
                    selected = isSelected,
                    onClick = { onModeChange(mode) },
                    text = {
                        Text(
                            text = when (mode) {
                                "Original" -> "Original H&E"
                                "Nuclear" -> "Nuclear View"
                                "Gland" -> "Gland View"
                                "Overlay" -> "Combined Overlay"
                                else -> mode
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Blue500 else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image Viewport Frame
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val realBitmap = ColonPathRepository.selectedBitmap
                if (realBitmap != null && selectedMode == "Original") {
                    Image(
                        bitmap = realBitmap.asImageBitmap(),
                        contentDescription = "Original Raw H&E",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else if (ColonPathRepository.currentCaseResult != null) {
                    when (selectedMode) {
                        "Nuclear" -> {
                            Image(
                                painter = painterResource(id = R.drawable.hovernet_nuclear),
                                contentDescription = "HoVer-Net Nuclei",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Gland" -> {
                            Image(
                                painter = painterResource(id = R.drawable.hovernet_gland),
                                contentDescription = "U-Net Glands",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "Overlay" -> {
                            Image(
                                painter = painterResource(id = R.drawable.hovernet_overlay),
                                contentDescription = "Combined Overlay",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else -> {
                            Text("Select a visualization layer above", color = TextSecondary)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No active specimen loaded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Upload an H&E image to begin analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
