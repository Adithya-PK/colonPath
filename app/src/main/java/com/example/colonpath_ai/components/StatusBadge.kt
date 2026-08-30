package com.example.colonpath_ai.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.colonpath_ai.model.CaseStatus
import com.example.colonpath_ai.model.QualityStatus
import com.example.colonpath_ai.ui.theme.*

@Composable
fun StatusBadge(status: CaseStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        CaseStatus.COMPLETED -> Triple(GreenLight, GreenSuccess, "Completed")
        CaseStatus.PENDING_REVIEW -> Triple(AmberLight, AmberWarning, "Pending Review")
        CaseStatus.IN_PROGRESS -> Triple(Blue50, Blue500, "In Progress")
        CaseStatus.PENDING -> Triple(Color(0xFFE0E0E0), Color(0xFF757575), "Pending")
        CaseStatus.FAILED -> Triple(RedLight, RedError, "Failed")
    }
    
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun QualityBadge(status: QualityStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        QualityStatus.GOOD -> Triple(GreenLight, GreenSuccess, "Good")
        QualityStatus.NEEDS_REVIEW -> Triple(AmberLight, AmberWarning, "Needs Review")
        QualityStatus.REJECTED -> Triple(RedLight, RedError, "Rejected")
    }
    
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
