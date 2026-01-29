package com.example.resqlink.ui.feature_sos.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

class SosInboxViewModelFactory(
    private val reachControlUseCase: ReachControlUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SosInboxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SosInboxViewModel(reachControlUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
