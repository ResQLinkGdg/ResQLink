package com.example.resqlink.platform.reach.protocol.sos

import kotlinx.serialization.Serializable

@Serializable
enum class SosSituation {
    TRAPPED,
    BLEEDING,
    CARDIAC,
    ISOLATED,
    FIRE,
    OTHER
}
