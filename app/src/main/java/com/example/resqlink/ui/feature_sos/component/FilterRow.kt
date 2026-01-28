package com.example.resqlink.ui.feature_sos.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqlink.ui.feature_sos.model.SosFilter

@Composable
fun FilterRow(
    selected: SosFilter,
    onSelect: (SosFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipX("전체", selected == SosFilter.ALL) {
            onSelect(SosFilter.ALL)
        }
        FilterChipX("긴급", selected == SosFilter.URGENT) {
            onSelect(SosFilter.URGENT)
        }
        FilterChipX("근처", selected == SosFilter.NEAR) {
            onSelect(SosFilter.NEAR)
        }
        FilterChipX("최신", selected == SosFilter.RECENT) {
            onSelect(SosFilter.RECENT)
        }
    }
}

@Composable
private fun FilterChipX(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}
