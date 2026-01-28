package com.example.resqlink.ui.feature_responder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

/**
 * RadarViewModel 계약(네가 이미 만들어둔 ViewModel에 맞춰서 함수/프로퍼티 이름만 맞춰줘)
 *
 * - uiState: RadarUiState를 StateFlow로 expose
 * - onEnter(): 화면 들어올 때 startReachMode 같은 거
 * - onExit(): 화면 나갈 때 stop/shutdown 같은 거
 * - setMode(mode): GPS_ON/OFF 전환
 * - select(key): 점 클릭 시 상세 표시용
 */
@Composable
fun RadarRoute(
    factory: RadarViewModelFactory
) {
    val vm: RadarViewModel = viewModel(factory = factory)
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        vm.onEnterScreen()
        onDispose { vm.onExitScreen() }
    }

    RadarScreen(
        uiState = uiState,
        onToggleMode = { mode -> vm.onToggleGps(mode == RadarMode.GPS_ON) },
        onSelectKey = { key -> vm.select(key) },
        onDismissSelection = { vm.select(null) }
    )
}

