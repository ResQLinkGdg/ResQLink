package com.example.resqlink.platform.reach.protocol

import kotlinx.serialization.Serializable

@Serializable
data class HopSignal(
    val from: String,          // 이전 senderId
    val to: String,            // 현재 노드 senderId
    val rssi: Int?,            // nullable
    val timestampMs: Long
)
