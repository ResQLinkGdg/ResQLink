package com.example.resqlink.ui.feature_sos.inbox.model

enum class RiskLevel {
    HIGH, MID, LOW
}

enum class SosFilter {
    ALL, URGENT, NEAR, RECENT
}

data class SosReportUiModel(
    val id: String,
    val risk: RiskLevel,
    val category: String,
    val title: String,
    val minutesAgo: Int,
    val distanceM: Int,
    val signalText: String
)
