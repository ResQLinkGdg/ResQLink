package com.example.resqlink.ui.feature_responder

import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.model.radar.RadarTarget

data class RadarUiState(
    val mode: RadarMode = RadarMode.GPS_OFF,
    val signals: List<RadarSignalUi> = emptyList(),
    val selectedKey: String? = null
)