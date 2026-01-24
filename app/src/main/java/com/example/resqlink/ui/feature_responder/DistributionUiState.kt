package com.example.resqlink.ui.feature_responder

import com.example.resqlink.domain.model.distribution.DistributionPoint
import com.example.resqlink.domain.model.distribution.DistributionSnapshot

data class DistributionUiState(
    val isLoading: Boolean = false,
    val snapshot: DistributionSnapshot? = null,

    // history (옵션)
    val showHistory: Boolean = false,
    val historyMinutes: Int = 30,
    val history: List<DistributionPoint> = emptyList()
)
