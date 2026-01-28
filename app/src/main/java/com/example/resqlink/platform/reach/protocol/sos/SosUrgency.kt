package com.example.resqlink.platform.reach.protocol.sos

import kotlinx.serialization.Serializable

@Serializable
enum class SosUrgency {
    HIGH,
    MEDIUM,
    LOW
}
