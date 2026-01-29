package com.example.resqlink.ui.feature_sos.compose.model

data class SosComposeUiState(
    val urgency: SosUrgencyUi? = null,
    val situation: SosSituationUi? = null,
    val peopleCount: Int = 1,
    val hint: String = "",
    val includeLocation: Boolean = true,
    val sending: Boolean = false
) {
    val canSend: Boolean
        get() = urgency != null && situation != null && !sending
}
