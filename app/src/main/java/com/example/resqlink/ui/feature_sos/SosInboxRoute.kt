package com.example.resqlink.ui.feature_sos

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqlink.ui.feature_sos.model.*

@Composable
fun SosInboxRoute(
    viewModel: SosInboxViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val filtered = remember(state.reports, state.selectedFilter) {
        when (state.selectedFilter) {
            SosFilter.ALL -> state.reports
            SosFilter.URGENT -> state.reports.filter { it.risk == RiskLevel.HIGH }
            SosFilter.NEAR -> state.reports.filter { it.distanceM <= 50 }
            SosFilter.RECENT -> state.reports.filter { it.minutesAgo <= 10 }
        }
    }

    SosInboxScreen(
        state = state,
        reports = filtered,
        onSelectFilter = viewModel::selectFilter,
        onClickSos = viewModel::onClickSos
    )
}
