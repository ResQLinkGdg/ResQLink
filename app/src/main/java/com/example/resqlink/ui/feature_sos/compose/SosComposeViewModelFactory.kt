package com.example.resqlink.ui.feature_sos.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

class SosComposeViewModelFactory(
    private val reachControlUseCase: ReachControlUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SosComposeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SosComposeViewModel(reachControlUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
