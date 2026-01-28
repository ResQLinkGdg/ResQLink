package com.example.resqlink.ui.feature_sos.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopStatusBar(
    isDisasterMode: Boolean,
    onToggleDisasterMode: (Boolean) -> Unit,
    onClickRadar: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDisasterMode)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.outline
                    )
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "재난 모드",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.weight(1f))

            Switch(
                checked = isDisasterMode,
                onCheckedChange = onToggleDisasterMode
            )

            IconButton(onClick = onClickRadar) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "Radar")
            }
        }

    }
}
