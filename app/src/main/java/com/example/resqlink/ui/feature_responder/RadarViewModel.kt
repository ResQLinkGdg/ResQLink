package com.example.resqlink.ui.feature_responder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.usecase.radar.RefreshMyLocationUsecase
import com.example.resqlink.domain.usecase.radar.SetRadarModeUsecase
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RadarViewModel(
    private val store: RadarStateStore,
    private val reachControl: ReachControlUseCase,
    private val setRadarMode: SetRadarModeUsecase,
    private val refreshMyLocation: RefreshMyLocationUsecase,
) : ViewModel() {

    private var gpsPollingJob: Job? = null

    // UI에서 선택된 점의 키를 관리하는 StateFlow 추가
    private val _selectedKey = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RadarUiState> =
        combine(store.mode, store.targets, _selectedKey) { mode, targets, selectedKey ->
            RadarUiState(
                mode = mode,
                signals = targets.map { target ->
                    // RadarTarget(도메인) -> RadarSignalUi(UI용) 변환 로직
                    RadarSignalUi(
                        key = target.key,
                        bucket = target.bucket,
                        bearingDeg = target.bearingDeg,
                        displayRange = "약 ${target.approxRangeText}",
                        lastSeenMs = target.lastSeenMs
                    )
                },
                selectedKey = selectedKey
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RadarUiState()
        )

    /** 점 클릭 시 호출될 함수 추가 */
    fun select(key: String?) {
        _selectedKey.value = key
    }

    /** 화면 진입 시 호출(Compose에서 DisposableEffect로 호출 추천) */
    fun onEnterScreen() {
        reachControl.startReachMode()
        // GPS 모드라면 위치 폴링 시작
        if (store.mode.value == RadarMode.GPS_ON) startGpsPolling()
    }

    /** 화면 이탈 시 호출 */
    fun onExitScreen() {
        stopGpsPolling()
        // “화면 나가면 종료” 정책일 때만!
        reachControl.stopReachMode()
    }

    fun onToggleGps(enabled: Boolean) {
        val mode = if (enabled) RadarMode.GPS_ON else RadarMode.GPS_OFF
        setRadarMode(mode)

        if (mode == RadarMode.GPS_ON) startGpsPolling()
        else stopGpsPolling()
    }

    fun onSendSos(ttl: Int, text: String?) {
        viewModelScope.launch {
            reachControl.sendSos(ttl = ttl, text = text)
        }
    }

    private fun startGpsPolling() {
        if (gpsPollingJob?.isActive == true) return
        gpsPollingJob = viewModelScope.launch {
            while (true) {
                refreshMyLocation()
                delay(2_000)
            }
        }
    }

    private fun stopGpsPolling() {
        gpsPollingJob?.cancel()
        gpsPollingJob = null
    }

    override fun onCleared() {
        // 화면/VM 수명주기 정책에 맞게 선택
        stopGpsPolling()
        // reachControl.stopReachMode()  // <- “VM이 사라질 때 종료” 정책이면 켜기
        super.onCleared()
    }
}