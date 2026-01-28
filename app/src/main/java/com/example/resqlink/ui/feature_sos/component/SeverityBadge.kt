package com.example.resqlink.ui.feature_sos.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.resqlink.ui.feature_sos.inbox.model.RiskLevel

@Composable
fun SeverityBadge(level: RiskLevel) {
    val (label, bgColor) = when (level) {
        RiskLevel.HIGH -> "HIGH" to MaterialTheme.colorScheme.errorContainer
        RiskLevel.MID -> "MID" to MaterialTheme.colorScheme.tertiaryContainer
        RiskLevel.LOW -> "LOW" to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
