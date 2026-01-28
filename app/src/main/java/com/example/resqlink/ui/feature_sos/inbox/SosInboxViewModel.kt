package com.example.resqlink.ui.feature_sos.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase
import com.example.resqlink.ui.feature_sos.inbox.mapper.toUiModel
import com.example.resqlink.ui.feature_sos.inbox.model.SosFilter
import com.example.resqlink.ui.feature_sos.inbox.model.SosUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SosInboxViewModel(
    private val reachControlUseCase: ReachControlUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SosUiState())
    val state: StateFlow<SosUiState> = _state.asStateFlow()

    init {
        observeIncomingSos()
    }

    private fun observeIncomingSos() {
        viewModelScope.launch {
            reachControlUseCase.incomingSosFlow.collect { sos ->
                val uiModel = sos.toUiModel()

                _state.update {
                    it.copy(
                        reports = listOf(uiModel) + it.reports
                    )
                }
            }
        }
    }

    /** ✅ 필터 선택 */
    fun selectFilter(filter: SosFilter) {
        _state.update {
            it.copy(selectedFilter = filter)
        }
    }

    fun toggleDisasterMode(enabled: Boolean) {
        if (enabled) {
            reachControlUseCase.startReachMode()
        } else {
            reachControlUseCase.stopReachMode()
        }

        _state.update {
            it.copy(isDisasterMode = enabled)
        }
    }
}
