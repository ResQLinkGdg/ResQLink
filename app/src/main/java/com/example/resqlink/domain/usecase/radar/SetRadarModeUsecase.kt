package com.example.resqlink.domain.usecase.radar

import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.model.radar.RadarMode

class SetRadarModeUsecase(
    private val store: RadarStateStore
) {
    operator fun invoke(mode: RadarMode) {
        store.setMode(mode)
    }
}