package com.example.resqlink.ui.feature_sos.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.resqlink.data.store.IdentityStore
import com.example.resqlink.domain.usecase.radar.ApplyIncomingSosUsecase
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

class SosComposeViewModelFactory(
    private val reachControlUseCase: ReachControlUseCase,
    private val identityStore: IdentityStore
) : ViewModelProvider.Factory {
    val myId = identityStore.getMyId()


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SosComposeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SosComposeViewModel(reachControlUseCase, identityStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
