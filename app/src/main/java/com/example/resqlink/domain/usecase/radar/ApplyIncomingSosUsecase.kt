package com.example.resqlink.domain.usecase.radar

import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.platform.reach.protocol.MessageEnvelope
import com.example.resqlink.platform.reach.protocol.MessageType
import com.example.resqlink.platform.reach.protocol.SosPayload

class ApplyIncomingSosUsecase(
    private val store: RadarStateStore,
    private val locationProvider: LocationProvider
) {
    suspend operator fun invoke(envelope: MessageEnvelope, rssiDbm: Int?) {
        if (envelope.type != MessageType.SOS) return

        val payload = envelope.payload as? SosPayload ?: return
        val payloadLoc: GeoLocation? =
            if (payload.lat != null && payload.lng != null) GeoLocation(payload.lat, payload.lng) else null

        val myLoc: GeoLocation? =
            if (store.mode.value == RadarMode.GPS_ON) locationProvider.getCurrentLocation()
            else null

        val originId = extractOriginId(envelope)

        store.onIncomingSos(
            originId = originId,
            msgId = envelope.msgId,
            rssiDbm = rssiDbm,
            payloadLocation = payloadLoc,
            myLocation = myLoc
        )
    }

    /**
     * 너 코드 기준: relay가 senderId를 덮어쓰므로 origin은 hops에서 복원.
     * - 원본 송신: hops 비어있음 -> senderId가 origin
     * - 릴레이된 것: hops[0].from이 최초 origin
     */
    private fun extractOriginId(envelope: MessageEnvelope): String {
        val firstHopFrom = envelope.hops.firstOrNull()?.from
        return firstHopFrom ?: envelope.senderId
    }
}