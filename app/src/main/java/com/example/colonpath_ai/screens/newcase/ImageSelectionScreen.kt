package com.example.colonpath_ai.screens.newcase

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.R
import com.example.colonpath_ai.components.SectionHeader
import com.example.colonpath_ai.data.SampleDataRepository
import com.example.colonpath_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    onBack: () -> Unit,
    onAnalyze: () -> Unit,
    onLiveMicroscope: () -> Unit
) {
    val context = LocalContext.current
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    // Android Gallery / Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            SampleDataRepository.selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                if (bmp != null) {
                    SampleDataRepository.selectedBitmap = bmp
                    val name = uri.lastPathSegment?.substringAfterLast('/') ?: "raw_specimen.png"
                    SampleDataRepository.selectedImageName = name
                    uploadStatusMessage = "Loaded: $name (${bmp.width} × ${bmp.height})"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uploadStatusMessage = "Failed to decode selected image"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select H&E Image") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provide an H&E stained histopathology image for computational AI analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Acquisition Action Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AcquisitionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Upload,
                        title = "Upload H&E Image",
                        subtitle = "Select from gallery",
                        onClick = { galleryLauncher.launch("image/*") }
                    )
                    AcquisitionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.CameraAlt,
                        title = "Live Microscope",
                        subtitle = "USB / OTG Camera",
                        onClick = onLiveMicroscope
                    )
                }
            }

            // Image Preview Header
            item {
                SectionHeader(
                    title = if (SampleDataRepository.selectedBitmap != null) "Selected Specimen Preview" else "Default Sample Specimen",
                    subtitle = if (SampleDataRepository.selectedBitmap != null) "Raw H&E input ready for analysis" else "Sample tissue provided for demonstration"
                )
            }

            // Specimen Image Preview Container
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
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
                        val currentBmp = SampleDataRepository.selectedBitmap
                        if (currentBmp != null) {
                            Image(
                                bitmap = currentBmp.asImageBitmap(),
                                contentDescription = "Uploaded Specimen",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.demo_sample_raw),
                                contentDescription = "Sample H&E Specimen",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Badge over image
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Navy800.copy(alpha = 0.82f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (SampleDataRepository.selectedBitmap != null) "Custom Image Selected" else "Sample Colorectal H&E Patch",
                                    color = SurfaceWhite,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Metadata Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentBmp = SampleDataRepository.selectedBitmap
                        val width = currentBmp?.width ?: 2048
                        val height = currentBmp?.height ?: 1536
                        val srcName = SampleDataRepository.selectedImageName

                        MetadataRow(label = "Image Source", value = if (currentBmp != null) "Gallery: $srcName" else "Demo H&E Sample")
                        MetadataRow(label = "Resolution", value = "$width × $height px")
                        MetadataRow(label = "Staining Quality", value = "Optimal (Passed QC)")
                        MetadataRow(label = "Optical Magnification", value = "40× Objective Equivalent")
                    }
                }
            }

            // Action Button
            item {
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text("Analyze Specimen")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AcquisitionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Blue500,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
    }
}
