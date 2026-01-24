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

    fun newRespond(
        senderId: String,
        sosMsgId: String,
        ttl: Int = 0, // 기본: respond는 멀티홉 안 할 거라 0 권장
        timestampMs: Long = System.currentTimeMillis(),
        etaMinutes: Int? = null,
        note: String? = null
    ): MessageEnvelope =
        MessageEnvelope(
            type = MessageType.RESPOND,
            msgId = UUID.randomUUID().toString(),
            senderId = senderId,
            timestampMs = timestampMs,
            ttl = ttl,
            hops = emptyList(), // ⭐ 명시
            payload = RespondPayload(sosMsgId = sosMsgId, etaMinutes = etaMinutes, note = note)
        )
}
