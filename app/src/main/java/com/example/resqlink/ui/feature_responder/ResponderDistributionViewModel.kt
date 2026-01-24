package com.example.resqlink.ui.feature_responder

import androidx.lifecycle.ViewModel
import com.example.resqlink.domain.usecase.AppendDistributionHistoryUseCase

import com.example.resqlink.domain.usecase.GetDistributionHistoryUseCase
import com.example.resqlink.domain.usecase.GetDistributionSnapshotUseCase

class ResponderDistributionViewModel(
    private val getSnapshot: GetDistributionSnapshotUseCase,
    private val appendHistory: AppendDistributionHistoryUseCase,
    private val getHistory: GetDistributionHistoryUseCase,
) : ViewModel() {

    var state: DistributionUiState = DistributionUiState()
        private set

    fun refresh(now: Long = System.currentTimeMillis(), windowMinutes: Int = 5) {
        state = state.copy(isLoading = true)

        val snap = getSnapshot.execute(now = now, windowMinutes = windowMinutes)

        // 히스토리에 기록 (레이어 OK: ViewModel -> UseCase -> Store)
        appendHistory.execute(snap)

        // 히스토리 UI가 켜져있으면 같이 조회해서 state에 담기
        val historySeries = if (state.showHistory) {
            getHistory.execute(now = now, minutes = state.historyMinutes)
        } else emptyList()

        state = state.copy(
            snapshot = snap,
            history = historySeries,
            isLoading = false
        )
    }

    fun setShowHistory(show: Boolean, now: Long = System.currentTimeMillis()) {
        state = state.copy(showHistory = show)
        if (show) {
            state =
                state.copy(history = getHistory.execute(now = now, minutes = state.historyMinutes))
        } else {
            state = state.copy(history = emptyList())
        }
    }

    fun setHistoryMinutes(minutes: Int, now: Long = System.currentTimeMillis()) {
        state = state.copy(historyMinutes = minutes)
        if (state.showHistory) {
            state = state.copy(history = getHistory.execute(now = now, minutes = minutes))
        }
    }
}