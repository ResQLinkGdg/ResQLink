package com.example.resqlink.ui.feature_sos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.resqlink.ui.feature_sos.model.*

class SosInboxViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        SosUiState(
            reports = sampleReports()
        )
    )
    val state: StateFlow<SosUiState> = _state.asStateFlow()

    fun selectFilter(filter: SosFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun onClickSos() {
        // TODO: SOS 전송
    }
}

private fun sampleReports() = listOf(
    SosReportUiModel(
        id = "1",
        risk = RiskLevel.HIGH,
        category = "매몰",
        title = "B2 주차장 기둥 옆에 갇혔어요",
        minutesAgo = 1,
        distanceM = 15,
        signalText = "신호 양호"
    )
)
