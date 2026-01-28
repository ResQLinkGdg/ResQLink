package com.example.resqlink.platform.reach.protocol

import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency
import java.util.UUID

object MessageFactory {

    fun newSos(
        senderId: String,
        ttl: Int,
        urgency: SosUrgency,
        situation: SosSituation,
        peopleCount: Int?,
        hint: String?,
        lat: Double?,
        lng: Double?,
        timestampMs: Long = System.currentTimeMillis()
    ): MessageEnvelope =
        MessageEnvelope(
            type = MessageType.SOS,
            msgId = UUID.randomUUID().toString(),
            senderId = senderId,
            timestampMs = timestampMs,
            ttl = ttl,
            hops = emptyList(),
            payload = SosPayload(
                urgency = urgency,
                situation = situation,
                peopleCount = peopleCount,
                hint = hint,
                lat = lat,
                lng = lng
            )
        )
}
