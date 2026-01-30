package com.example.resqlink.domain.usecase.radar

import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.model.sos.IncomingSosEvent
import com.example.resqlink.platform.reach.protocol.MessageEnvelope
import com.example.resqlink.platform.reach.protocol.MessageType
import com.example.resqlink.platform.reach.protocol.SosPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class ApplyIncomingSosUsecase(
    private val store: RadarStateStore,
    private val locationProvider: LocationProvider,
    private val mySenderId: String
) {

    private val _incomingSosEvents =
        MutableSharedFlow<IncomingSosEvent>(
            extraBufferCapacity = 64
        )

    val incomingSosFlow: SharedFlow<IncomingSosEvent>
        get() = _incomingSosEvents

    suspend operator fun invoke(
        envelope: MessageEnvelope,
        rssiDbm: Int?
    ) {
        if (envelope.type != MessageType.SOS) return

        val senderId= extractOriginId(envelope)
        //  필터링 추가: 이 메시지를 처음 만든 사람이 '나'라면 레이더에 표시하지 않음
        if (senderId == mySenderId) {
            return
        }

        val payload = envelope.payload as? SosPayload ?: return

        val payloadLoc =
            if (payload.lat != null && payload.lng != null)
                GeoLocation(payload.lat, payload.lng)
            else null

        val myLoc =
            if (store.mode.value == RadarMode.GPS_ON)
                locationProvider.getCurrentLocation()
            else null

        val originId = extractOriginId(envelope)

        // ✅ 기존 Radar 로직 (유지)

        store.onIncomingSos(
            originId = originId,
            msgId = envelope.msgId,
            rssiDbm = rssiDbm,
            payloadLocation = payloadLoc,
            myLocation = myLoc
        )

        // ✅ Inbox / UI 용 이벤트 방출
        _incomingSosEvents.tryEmit(
            IncomingSosEvent(
                originId = originId,
                msgId = envelope.msgId,
                urgency = payload.urgency,
                situation = payload.situation,
                peopleCount = payload.peopleCount,
                hint = payload.hint,
                payloadLocation = payloadLoc,
                rssiDbm = rssiDbm,
                timestampMs = envelope.timestampMs,
                hops = envelope.hops
            )
        )
    }

    private fun extractOriginId(envelope: MessageEnvelope): String {
        val firstHopFrom = envelope.hops.firstOrNull()?.from
        return firstHopFrom ?: envelope.senderId
    }
}