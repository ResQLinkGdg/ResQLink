package com.example.resqlink.ui.feature_sos.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.resqlink.ui.feature_sos.inbox.model.SosReportUiModel

@Composable
fun ReportCard(item: SosReportUiModel) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                SeverityBadge(item.risk)
                Spacer(Modifier.width(8.dp))
                Text(item.category, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${item.minutesAgo}분 전", fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("${item.distanceM}m · ${item.signalText}", fontSize = 12.sp)
        }
    }
}
