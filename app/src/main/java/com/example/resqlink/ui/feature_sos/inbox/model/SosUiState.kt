package com.example.resqlink.ui.feature_sos.inbox.model

data class SosUiState(
    val reports: List<SosReportUiModel> = emptyList(),
    val selectedFilter: SosFilter = SosFilter.ALL,
    val isDisasterMode: Boolean = false
)
