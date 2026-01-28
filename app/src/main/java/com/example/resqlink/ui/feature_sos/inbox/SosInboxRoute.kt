package com.example.resqlink.ui.feature_sos.inbox

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase
import com.example.resqlink.ui.feature_sos.inbox.model.RiskLevel
import com.example.resqlink.ui.feature_sos.inbox.model.SosFilter

@Composable
fun SosInboxRoute(
    reachControlUseCase: ReachControlUseCase,
    onOpenRadar: () -> Unit,
    onNavigateToCompose: () -> Unit
) {
    val viewModel: SosInboxViewModel = viewModel(
        factory = SosInboxViewModelFactory(reachControlUseCase)
    )

    val state by viewModel.state.collectAsState()

    val filtered = remember(state.reports, state.selectedFilter) {
        when (state.selectedFilter) {
            SosFilter.ALL -> state.reports
            SosFilter.URGENT -> state.reports.filter { it.risk == RiskLevel.HIGH }
            SosFilter.NEAR -> state.reports.filter { it.distanceM!! <= 50 }
            SosFilter.RECENT -> state.reports.filter { it.minutesAgo <= 10 }
        }
    }

    SosInboxScreen(
        state = state,
        reports = filtered,
        onSelectFilter = viewModel::selectFilter,
        onClickSos = onNavigateToCompose,
        onOpenRadar = onOpenRadar,
        onToggleDisasterMode = viewModel::toggleDisasterMode   // ✅ 여기서 연결
    )
}
