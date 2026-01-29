package com.example.resqlink.ui.feature_sos.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqlink.ui.feature_sos.component.FilterRow
import com.example.resqlink.ui.feature_sos.component.ReportCard
import com.example.resqlink.ui.feature_sos.component.SosBanner
import com.example.resqlink.ui.feature_sos.component.TopStatusBar
import com.example.resqlink.ui.feature_sos.inbox.model.SosFilter
import com.example.resqlink.ui.feature_sos.inbox.model.SosReportUiModel
import com.example.resqlink.ui.feature_sos.inbox.model.SosUiState

@Composable
fun SosInboxScreen(
    state: SosUiState,
    reports: List<SosReportUiModel>,
    onSelectFilter: (SosFilter) -> Unit,
    onClickSos: () -> Unit,
    onOpenRadar: () -> Unit,    // 👈 추가
    onToggleDisasterMode: (Boolean) -> Unit   // 👈 콜백만 받음
) {
    Scaffold(
        topBar = {
            TopStatusBar(
                isDisasterMode = state.isDisasterMode,
                onClickRadar = onOpenRadar,
                onToggleDisasterMode = onToggleDisasterMode
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
                //  id를 기준으로 중복을 제거한 리스트만 그립니다.
                items(reports.distinctBy { it.id }, key = { it.id }) {
                    ReportCard(it)
                }
            }
        }
    }
}
