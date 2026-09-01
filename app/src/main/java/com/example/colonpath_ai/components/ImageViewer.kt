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
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

@Composable
fun ImageViewer(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("Original", "Nuclear", "Gland", "Overlay")

    Column(modifier = modifier.fillMaxWidth()) {
        // Tab Row with clean pill indicator
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
                when (selectedMode) {
                    "Original" -> {
                        val customBmp = SampleDataRepository.selectedBitmap
                        if (customBmp != null) {
                            Image(
                                bitmap = customBmp.asImageBitmap(),
                                contentDescription = "Original Raw H&E",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.demo_sample_raw),
                                contentDescription = "Original Raw H&E",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    "Nuclear" -> {
                        Image(
                            painter = painterResource(id = R.drawable.hovernet_nuclear),
                            contentDescription = "Nuclear Analysis Mask",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    "Gland" -> {
                        Image(
                            painter = painterResource(id = R.drawable.hovernet_gland),
                            contentDescription = "Gland Architecture Contours",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    "Overlay" -> {
                        Image(
                            painter = painterResource(id = R.drawable.hovernet_overlay),
                            contentDescription = "HoVer-Net Combined Overlay",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Layer Annotation Badge at Bottom
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Navy800.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = when (selectedMode) {
                            "Original" -> "Raw Unprocessed H&E Biopsy Patch"
                            "Nuclear" -> "HoVer-Net Nuclei Segmentation (Green) & Centroids (Red/Blue)"
                            "Gland" -> "Glandular Lumen Segmentation & Boundary Contours"
                            "Overlay" -> "Combined HoVer-Net Cell & Architecture Overlay"
                            else -> selectedMode
                        },
                        color = SurfaceWhite,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
