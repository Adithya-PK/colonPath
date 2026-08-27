package com.example.colonpath_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ColonPathApp()
        }
    }
}


/* =====================================================
   MAIN APPLICATION
   ===================================================== */

@Composable
fun ColonPathApp() {

    var currentScreen by remember {
        mutableStateOf("dashboard")
    }

    when (currentScreen) {

        "dashboard" -> {

            ColonPathDashboard(
                onStartAnalysis = {
                    currentScreen = "image"
                }
            )
        }

        "image" -> {

            ImageSelectionScreen(
                onBack = {
                    currentScreen = "dashboard"
                },
                onAnalyze = {
                    currentScreen = "analysis"
                }
            )
        }

        "analysis" -> {

            AnalysisScreen(
                onBack = {
                    currentScreen = "image"
                },
                onComparison = {
                    currentScreen = "comparison"
                }
            )
        }

        "comparison" -> {

            ComparisonScreen(
                onBack = {
                    currentScreen = "analysis"
                },
                onReport = {
                    currentScreen = "report"
                }
            )
        }

        "report" -> {

            ReportScreen(
                onBack = {
                    currentScreen = "comparison"
                }
            )
        }
    }
}


/* =====================================================
   DASHBOARD
   ===================================================== */

@Composable
fun ColonPathDashboard(
    onStartAnalysis: () -> Unit
) {

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF6F8FB)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {

                item {

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )


                    /* -------------------------------------
                       APP HEADER
                       ------------------------------------- */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F0FE)
                        ) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "CP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2457A6)
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column {

                            Text(
                                text = "ColonPath-AI",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF172B4D)
                            )

                            Spacer(
                                modifier = Modifier.height(2.dp)
                            )

                            Text(
                                text = "AI-Assisted Colorectal Histopathology",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )


                    /* -------------------------------------
                       MAIN ANALYSIS CARD
                       ------------------------------------- */

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEAF2FF)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(22.dp)
                        ) {

                            Text(
                                text = "Colorectal Tissue Analysis",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF172B4D)
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Analyze H&E histopathology images using computer vision, quantitative morphology and AI-assisted interpretation.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = Color(0xFF46566F)
                            )

                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )

                            Button(
                                onClick = onStartAnalysis,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(13.dp)
                            ) {

                                Text(
                                    text = "Start Analysis",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )


                    /* -------------------------------------
                       SAMPLE CASE
                       ------------------------------------- */

                    Text(
                        text = "Sample Case",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172B4D)
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        text = "COL-2026-001",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "Colorectal Tissue",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFEAF6EA)
                                ) {

                                    Text(
                                        text = "Sample",
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF287A35)
                                    )
                                }
                            }


                            Spacer(
                                modifier = Modifier.height(15.dp)
                            )


                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                CaseDetail(
                                    title = "Stain",
                                    value = "H&E"
                                )

                                CaseDetail(
                                    title = "Image",
                                    value = "2048 × 1536"
                                )

                                CaseDetail(
                                    title = "Status",
                                    value = "Ready"
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(26.dp)
                    )


                    /* -------------------------------------
                       ANALYSIS PIPELINE
                       ------------------------------------- */

                    Text(
                        text = "Analysis Pipeline",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172B4D)
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    PipelineItem(
                        number = "01",
                        title = "Image Quality",
                        description = "Assess focus, contrast, staining and tissue coverage."
                    )

                    PipelineItem(
                        number = "02",
                        title = "Nuclear & Gland Analysis",
                        description = "Analyze cellular and glandular structures."
                    )

                    PipelineItem(
                        number = "03",
                        title = "Morphology",
                        description = "Calculate quantitative tissue characteristics."
                    )

                    PipelineItem(
                        number = "04",
                        title = "Reference Comparison",
                        description = "Compare the sample with reference tissue patterns."
                    )

                    PipelineItem(
                        number = "05",
                        title = "AI-Assisted Report",
                        description = "Present structured computational observations."
                    )


                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )


                    /* -------------------------------------
                       AI PIPELINE
                       ------------------------------------- */

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text = "AI Pipeline",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF172B4D)
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                SmallPipelineStep("CV")

                                SmallPipelineStep("Morphology")

                                SmallPipelineStep("Retrieval")

                                SmallPipelineStep("AI")
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )


                    /* -------------------------------------
                       DISCLAIMER
                       ------------------------------------- */

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF5E6)
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {

                            Text(
                                text = "⚠",
                                fontSize = 20.sp
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Column {

                                Text(
                                    text = "Prototype / Research Use",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B4F00)
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "AI-assisted decision support — not a substitute for professional diagnosis.",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF6B4F00)
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )
                }
            }
        }
    }
}


/* =====================================================
   CASE DETAIL
   ===================================================== */

@Composable
fun CaseDetail(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/* =====================================================
   PIPELINE ITEM
   ===================================================== */

@Composable
fun PipelineItem(
    number: String,
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F0FE)
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = number,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2457A6)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


/* =====================================================
   SMALL PIPELINE STEP
   ===================================================== */

@Composable
fun SmallPipelineStep(
    text: String
) {

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF1F4F8)
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 7.dp
            ),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF46566F)
        )
    }
}


/* =====================================================
   IMAGE SELECTION
   ===================================================== */

@Composable
fun ImageSelectionScreen(
    onBack: () -> Unit,
    onAnalyze: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "‹",
            fontSize = 40.sp,
            color = Color(0xFF172B4D),
            modifier = Modifier.clickable {
                onBack()
            }
        )

        Text(
            text = "Select H&E Image",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172B4D)
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Choose a colorectal histopathology image for analysis.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8C6D0)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "H&E",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B3045)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Sample Colorectal Tissue",
                    fontSize = 16.sp,
                    color = Color(0xFF6B3045)
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "Hematoxylin & Eosin",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Button(
            onClick = onAnalyze,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {

            Text(
                text = "Analyze Sample",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/* =====================================================
   AI TISSUE ANALYSIS
   ===================================================== */

@Composable
fun AnalysisScreen(
    onBack: () -> Unit,
    onComparison: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = "‹",
                fontSize = 40.sp,
                color = Color(0xFF172B4D),
                modifier = Modifier.clickable {
                    onBack()
                }
            )

            Text(
                text = "AI Tissue Analysis",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172B4D)
            )

            Text(
                text = "Prototype / Sample Output",
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF2FF)
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "AI Assessment",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Abnormal Morphology Pattern",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172B4D)
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Illustrative confidence: 87%",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Image Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE8C6D0)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "H&E",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B3045)
                    )

                    Text(
                        text = "Representative AI Annotation",
                        color = Color.DarkGray
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Prototype visualization",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            ResultCard(
                title = "Nuclear Analysis",
                description = "Increased nuclear atypia detected.",
                severity = "Moderate"
            )

            ResultCard(
                title = "Gland Segmentation",
                description = "Irregular gland architecture detected.",
                severity = "High"
            )

            ResultCard(
                title = "Cell Density",
                description = "Elevated cellular density.",
                severity = "68%"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Morphology Metrics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            MetricRow(
                name = "Nuclear Area",
                value = "145 px²"
            )

            MetricRow(
                name = "Circularity",
                value = "0.71"
            )

            MetricRow(
                name = "Cell Density",
                value = "68%"
            )

            MetricRow(
                name = "Gland Irregularity",
                value = "0.64"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onComparison,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text("Compare With Reference")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}


/* =====================================================
   RESULT CARD
   ===================================================== */

@Composable
fun ResultCard(
    title: String,
    description: String,
    severity: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = severity,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = description,
                color = Color.Gray
            )
        }
    }
}


/* =====================================================
   METRIC ROW
   ===================================================== */

@Composable
fun MetricRow(
    name: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = name,
            color = Color.DarkGray
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}


/* =====================================================
   REFERENCE COMPARISON
   ===================================================== */

@Composable
fun ComparisonScreen(
    onBack: () -> Unit,
    onReport: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = "‹",
                fontSize = 40.sp,
                modifier = Modifier.clickable {
                    onBack()
                }
            )

            Text(
                text = "Reference Comparison",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172B4D)
            )

            Text(
                text = "Prototype reference retrieval",
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            ComparisonCard(
                referenceId = "REF-021",
                similarity = "94.2%",
                category = "Adenoma-like morphology"
            )

            ComparisonCard(
                referenceId = "REF-034",
                similarity = "91.8%",
                category = "Adenocarcinoma-like morphology"
            )

            ComparisonCard(
                referenceId = "REF-011",
                similarity = "88.6%",
                category = "Adenomatous morphology"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Current Case vs Reference",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            MetricRow(
                name = "Nuclear Circularity",
                value = "0.71  vs  0.86"
            )

            MetricRow(
                name = "Cell Density",
                value = "68%  vs  42%"
            )

            MetricRow(
                name = "Gland Irregularity",
                value = "0.64  vs  0.31"
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEAF6EA)
                )
            ) {

                Text(
                    text = "Similarity scores represent retrieval similarity and are not diagnostic probabilities.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onReport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text("Generate AI-Assisted Report")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}


/* =====================================================
   COMPARISON CARD
   ===================================================== */

@Composable
fun ComparisonCard(
    referenceId: String,
    similarity: String,
    category: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = referenceId,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = similarity,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = category,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}


/* =====================================================
   AI-ASSISTED REPORT
   ===================================================== */

@Composable
fun ReportScreen(
    onBack: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        item {

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = "‹",
                fontSize = 40.sp,
                modifier = Modifier.clickable {
                    onBack()
                }
            )

            Text(
                text = "AI-Assisted Report",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172B4D)
            )

            Text(
                text = "Prototype / Sample Report",
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            ReportSection(
                title = "Case",
                content = "COL-2026-001\nColorectal Tissue\nH&E"
            )

            ReportSection(
                title = "Summary",
                content = "Quantitative analysis identified measurable variations in nuclear morphology and glandular architecture. These findings are presented as structured computational observations for professional review."
            )

            ReportSection(
                title = "Computational Findings",
                content = "• 1,824 nuclei detected\n• Predominant epithelial population\n• Mean nuclear area: 47.3 px²\n• Mean circularity: 0.72\n• Nuclear density: 139.2 / mm²\n\n• 146 glands detected\n• Mean gland area: 2,840 px²\n• Moderate architectural irregularity\n• Moderate crowding"
            )

            ReportSection(
                title = "Reference Comparison",
                content = "REF-021\nSimilarity: 94.2%\nCategory: Adenoma-like morphology\n\nSimilarity represents image retrieval similarity and is not a diagnostic probability."
            )

            ReportSection(
                title = "AI Interpretation",
                content = "The analyzed tissue demonstrates measurable morphological differences in nuclear and glandular features compared with selected reference patterns. These computational observations should be interpreted together with the complete histopathological context."
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE9E9)
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "⚠ PATHOLOGIST REVIEW REQUIRED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "ColonPath-AI provides AI-assisted analysis and evidence organization. Final interpretation and diagnosis must be performed by a qualified pathologist.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text("Back to Comparison")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}


/* =====================================================
   REPORT SECTION
   ===================================================== */

@Composable
fun ReportSection(
    title: String,
    content: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {

        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172B4D)
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color.DarkGray
        )
    }
}