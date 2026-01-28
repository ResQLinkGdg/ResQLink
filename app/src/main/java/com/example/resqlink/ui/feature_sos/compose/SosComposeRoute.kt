package com.example.resqlink.ui.feature_sos.compose

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.resqlink.data.store.IdentityStore
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

@Composable
fun SosComposeRoute(
    navController: NavController,
    reachControlUseCase: ReachControlUseCase,
    identityStore: IdentityStore
) {
    val viewModel: SosComposeViewModel = viewModel(
        factory = SosComposeViewModelFactory(reachControlUseCase, identityStore )
    )

    val state by viewModel.state.collectAsState()

    SosComposeScreen(
        state = state,

        // ----- 긴급도 / 상황 -----
        onSelectUrgency = viewModel::onSelectUrgency,
        onSelectSituation = viewModel::onSelectSituation,

        // ----- 인원 -----
        onIncreasePeople = viewModel::onIncreasePeople,
        onDecreasePeople = viewModel::onDecreasePeople,

        // ----- 힌트 / 위치 -----
        onHintChange = viewModel::onHintChange,
        onToggleLocation = viewModel::onToggleLocation,

        // ----- 액션 -----
        onClickSend = {
            viewModel.onClickSend {
                navController.popBackStack()
            }
        },
        onClickBack = {
            navController.popBackStack()
        }
    )
}
