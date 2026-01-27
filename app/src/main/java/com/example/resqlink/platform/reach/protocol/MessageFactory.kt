package com.example.resqlink.platform.reach.protocol

import java.util.UUID

object MessageFactory {
    fun newSos(
        senderId: String,
        ttl: Int,
        timestampMs: Long = System.currentTimeMillis(),
        lat: Double? = null,
        lng: Double? = null,
        text: String? = null
    ): MessageEnvelope =
        MessageEnvelope(
            type = MessageType.SOS,
            msgId = UUID.randomUUID().toString(),
            senderId = senderId,
            timestampMs = timestampMs,
            ttl = ttl,
            hops = emptyList(), // ⭐ 명시
            payload = SosPayload(lat = lat, lng = lng, text = text)
        )

}
