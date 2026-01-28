package com.example.resqlink.ui.feature_sos.inbox.model

data class SosUiState(
    val isDisasterMode: Boolean = true,
    val nearbyCount: Int = 3,
    val batteryPercent: Int = 38,
    val selectedFilter: SosFilter = SosFilter.ALL,
    val reports: List<SosReportUiModel> = emptyList()
)