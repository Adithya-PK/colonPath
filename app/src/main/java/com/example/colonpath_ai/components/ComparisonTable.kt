package com.example.colonpath_ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.model.ComparisonMetric
import com.example.colonpath_ai.ui.theme.*

@Composable
fun ComparisonTable(
    metrics: List<ComparisonMetric>,
    modifier: Modifier = Modifier,
    referenceHeader: String = "Reference",
    patientHeader: String = "Patient"
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue50)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Metric",
                    modifier = Modifier.weight(1.3f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = referenceHeader,
                    modifier = Modifier.weight(1.0f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    textAlign = TextAlign.End
                )
                Text(
                    text = patientHeader,
                    modifier = Modifier.weight(1.0f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Blue500,
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            // Table Rows
            metrics.forEachIndexed { index, metric ->
                val rowBackground = if (index % 2 == 0) SurfaceWhite else BackgroundLight
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = metric.name,
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = metric.referenceValue,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = metric.patientValue,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        textAlign = TextAlign.End
                    )
                }
                if (index < metrics.size - 1) {
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}
