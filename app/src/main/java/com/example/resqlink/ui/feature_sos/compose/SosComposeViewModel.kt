package com.example.resqlink.ui.feature_sos.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.data.store.IdentityStore
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase
import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency
import com.example.resqlink.ui.feature_sos.compose.model.SosComposeUiState
import com.example.resqlink.ui.feature_sos.compose.model.SosSituationUi
import com.example.resqlink.ui.feature_sos.compose.model.SosUrgencyUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SosComposeViewModel(
    private val reachControlUseCase: ReachControlUseCase,
    private val identityStore: IdentityStore
) : ViewModel() {

    private val _state = MutableStateFlow(SosComposeUiState())
    val state: StateFlow<SosComposeUiState> = _state.asStateFlow()

    /* ---------- UI 이벤트 ---------- */

    fun onSelectUrgency(urgency: SosUrgencyUi) {
        _state.update { it.copy(urgency = urgency) }
    }

    fun onSelectSituation(situation: SosSituationUi) {
        _state.update { it.copy(situation = situation) }
    }

    fun onIncreasePeople() {
        _state.update { it.copy(peopleCount = it.peopleCount + 1) }
    }

    fun onDecreasePeople() {
        _state.update {
            it.copy(peopleCount = maxOf(1, it.peopleCount - 1))
        }
    }

    fun onHintChange(hint: String) {
        _state.update {
            it.copy(hint = hint.take(40))
        }
    }

    fun onToggleLocation(include: Boolean) {
        _state.update { it.copy(includeLocation = include) }
    }

    /* ---------- 전송 ---------- */

    fun onClickSend(onSuccess: () -> Unit) {
        val current = state.value
        if (!current.canSend) return

        viewModelScope.launch {
            _state.update { it.copy(sending = true) }

            val myId = identityStore.getMyId()
            val ttl = calculateTtl(current.urgency!!)
            val urgency = mapUrgency(current.urgency)
            val situation = mapSituation(current.situation!!)

            runCatching {
                reachControlUseCase.sendSos(
                    ttl = ttl,
                    urgency = urgency,
                    situation = situation,
                    peopleCount = current.peopleCount,
                    hint = current.hint.ifBlank { null },
                    includeLocation = current.includeLocation
                )
            }.onSuccess {
                onSuccess()
            }.onFailure {
                _state.update { it.copy(sending = false) }
                // TODO: 에러 처리 (Snackbar 등)
            }
        }
    }

    /* ---------- 매핑 / 정책 ---------- */

    private fun calculateTtl(urgency: SosUrgencyUi): Int =
        when (urgency) {
            SosUrgencyUi.HIGH -> 10
            SosUrgencyUi.MEDIUM -> 6
            SosUrgencyUi.LOW -> 3
        }

    private fun mapUrgency(ui: SosUrgencyUi): SosUrgency =
        when (ui) {
            SosUrgencyUi.HIGH -> SosUrgency.HIGH
            SosUrgencyUi.MEDIUM -> SosUrgency.MEDIUM
            SosUrgencyUi.LOW -> SosUrgency.LOW
        }

    private fun mapSituation(ui: SosSituationUi): SosSituation =
        when (ui) {
            SosSituationUi.TRAPPED -> SosSituation.TRAPPED
            SosSituationUi.BLEEDING -> SosSituation.BLEEDING
            SosSituationUi.CARDIAC -> SosSituation.CARDIAC
            SosSituationUi.ISOLATED -> SosSituation.ISOLATED
            SosSituationUi.FIRE -> SosSituation.FIRE
            SosSituationUi.OTHER -> SosSituation.OTHER
        }
}
