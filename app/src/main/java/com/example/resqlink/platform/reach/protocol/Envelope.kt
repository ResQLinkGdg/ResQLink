package com.example.resqlink.platform.reach.protocol

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
    val payload: Payload
)

@Serializable
sealed interface Payload

@Serializable
@SerialName("SOS")
data class SosPayload(
    val lat: Double? = null,    // 위도
    val lng: Double? = null,    // 경도
    val text: String? = null    // 요청 자유 서술
) : Payload

@Serializable
@SerialName("RESPOND")
data class RespondPayload(
    val sosMsgId: String,           // original SOS msgId
    val etaMinutes: Int? = null,    // 도착까지 예상 시간
    val note: String? = null        // 추가 메세지
) : Payload
