package com.example.resqlink.ui.feature_responder

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

@Composable
fun ResponderDistributionScreen(
    vm: ResponderDistributionViewModel = viewModel() // DI 없으면 Factory 필요
) {
    val uiState by vm.state.collectAsStateWithLifecycle()

    // 최초 1회 로드
    LaunchedEffect(Unit) {
        vm.refresh()
    }

    // 여기서 uiState.snapshot / uiState.history로 UI 그리면 됨
    // 예: 레이더 + 토글 + (옵션) 히스토리 그래프
}