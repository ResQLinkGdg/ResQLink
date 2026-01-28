package com.example.resqlink.ui.feature_responder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.usecase.radar.RefreshMyLocationUsecase
import com.example.resqlink.domain.usecase.radar.SetRadarModeUsecase
import com.example.resqlink.domain.usecase.reach.ReachControlUseCase

class RadarViewModelFactory(
    private val store: RadarStateStore,
    private val reachControl: ReachControlUseCase,
    private val setRadarMode: SetRadarModeUsecase,
    private val refreshMyLocation: RefreshMyLocationUsecase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RadarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RadarViewModel(
                store = store,
                reachControl = reachControl,
                setRadarMode = setRadarMode,
                refreshMyLocation = refreshMyLocation
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}