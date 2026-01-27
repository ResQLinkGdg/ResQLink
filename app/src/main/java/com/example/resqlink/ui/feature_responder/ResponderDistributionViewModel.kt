package com.example.resqlink.ui.feature_responder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.domain.usecase.AppendDistributionHistoryUseCase

import com.example.resqlink.domain.usecase.GetDistributionHistoryUseCase
import com.example.resqlink.domain.usecase.GetDistributionSnapshotUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResponderDistributionViewModel(
    private val getSnapshot: GetDistributionSnapshotUseCase,
    private val appendHistory: AppendDistributionHistoryUseCase,
    private val getHistory: GetDistributionHistoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DistributionUiState())
    val state: StateFlow<DistributionUiState> = _state

    fun refresh(now: Long = System.currentTimeMillis(), windowMinutes: Int = 5) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // ⚠️ 동기 유스케이스면 Default에서 수행(메인 스레드 블로킹 방지)
                val snap = withContext(Dispatchers.Default) {
                    getSnapshot.execute(now = now, windowMinutes = windowMinutes)
                }

                // 히스토리에 기록
                withContext(Dispatchers.Default) {
                    appendHistory.execute(snap)
                }

                //  히스토리 UI가 켜져 있으면 조회
                val historySeries = if (_state.value.showHistory) {
                    withContext(Dispatchers.Default) {
                        getHistory.execute(now = now, minutes = _state.value.historyMinutes)
                    }
                } else emptyList()

                _state.update {
                    it.copy(
                        snapshot = snap,
                        history = historySeries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "unknown error"
                    )
                }
            }
        }
    }

    fun setShowHistory(show: Boolean, now: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            _state.update { it.copy(showHistory = show, errorMessage = null) }

            if (!show) {
                _state.update { it.copy(history = emptyList()) }
                return@launch
            }

            // show=true면 즉시 불러오기
            try {
                val series = withContext(Dispatchers.Default) {
                    getHistory.execute(now = now, minutes = _state.value.historyMinutes)
                }
                _state.update { it.copy(history = series) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "history load error") }
            }
        }
    }

    fun setHistoryMinutes(minutes: Int, now: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            _state.update { it.copy(historyMinutes = minutes, errorMessage = null) }

            if (!_state.value.showHistory) return@launch

            try {
                val series = withContext(Dispatchers.Default) {
                    getHistory.execute(now = now, minutes = minutes)
                }
                _state.update { it.copy(history = series) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "history load error") }
            }
        }
    }
}