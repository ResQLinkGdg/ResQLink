package com.example.resqlink.platform.reach.protocol

import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageEnvelope(
    val v: Int = 1,                 // version
    val type: MessageType,
    val msgId: String,              // UUID string
    val senderId: String,           // stable device hash/uuid
    val timestampMs: Long,          // epoch millis
    val ttl: Int,                   // hop limit
    val hops: List<HopSignal> = emptyList(), // ⭐ rssi 정보를 담는 리스트 추가
    val payload: Payload
)

@Serializable
sealed interface Payload

@Serializable
@SerialName("SOS")
data class SosPayload(
    val urgency: SosUrgency,          // ⭐ 긴급도
    val situation: SosSituation,      // ⭐ 상황
    val peopleCount: Int? = null,     // ⭐ 대략 인원
    val hint: String? = null,         // ⭐ 추가 힌트 (40자 이내)
    val lat: Double? = null,           // 위치 (옵션)
    val lng: Double? = null
) : Payload

