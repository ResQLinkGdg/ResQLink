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
    val lat: Double? = null,
    val lng: Double? = null,
    val text: String? = null
) : Payload

@Serializable
@SerialName("RESPOND")
data class RespondPayload(
    val sosMsgId: String,           // original SOS msgId
    val etaMinutes: Int? = null,
    val note: String? = null
) : Payload
