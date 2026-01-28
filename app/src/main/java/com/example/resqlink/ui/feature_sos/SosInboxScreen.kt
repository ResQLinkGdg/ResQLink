package com.example.resqlink.ui.feature_sos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqlink.ui.common.component.BottomNavBar
import com.example.resqlink.ui.common.model.BottomTab
import com.example.resqlink.ui.feature_sos.component.FilterRow
import com.example.resqlink.ui.feature_sos.component.ReportCard
import com.example.resqlink.ui.feature_sos.component.SosBanner
import com.example.resqlink.ui.feature_sos.component.TopStatusBar
import com.example.resqlink.ui.feature_sos.model.SosFilter
import com.example.resqlink.ui.feature_sos.model.SosReportUiModel
import com.example.resqlink.ui.feature_sos.model.SosUiState

@Composable
fun SosInboxScreen(
    state: SosUiState,
    reports: List<SosReportUiModel>,
    onSelectFilter: (SosFilter) -> Unit,
    onClickSos: () -> Unit,
    onOpenRadar: () -> Unit    // 👈 추가
) {
    Scaffold(
        topBar = {
            TopStatusBar(
                isDisasterMode = state.isDisasterMode,
                nearbyCount = state.nearbyCount,
                batteryPercent = state.batteryPercent,
                onClickRadar = onOpenRadar
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SosBanner(
                modifier = Modifier.padding(16.dp),
                onClick = onClickSos
            )

            FilterRow(
                selected = state.selectedFilter,
                onSelect = onSelectFilter,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports, key = { it.id }) {
                    ReportCard(it)
                }
            }
        }
    }
}
