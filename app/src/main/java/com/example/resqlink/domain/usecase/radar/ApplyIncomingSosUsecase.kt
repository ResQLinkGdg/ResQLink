package com.example.resqlink.domain.usecase.radar

import android.util.Log
import com.example.resqlink.data.store.RadarStateStore
import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.gateway.RssiDistanceLogger
import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.model.sos.IncomingSosEvent
import com.example.resqlink.platform.reach.protocol.MessageEnvelope
import com.example.resqlink.platform.reach.protocol.MessageType
import com.example.resqlink.platform.reach.protocol.SosPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class ApplyIncomingSosUsecase(
    private val store: RadarStateStore,
    private val locationProvider: LocationProvider,
    private val mySenderId: String,
    private val rssiDistanceLogger: RssiDistanceLogger? = null
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

        val originId = extractOriginId(envelope)
        Log.d("ResQLink_Apply", "[가공] OriginId: $originId, MsgId: ${envelope.msgId}")

        if (originId == mySenderId) {
            Log.d("ResQLink_Apply", "[필터] 내가 최초 발신한 SOS입니다.")
            return
        }

        val payload = envelope.payload as? SosPayload ?: return

        val payloadLoc =
            if (payload.lat != null && payload.lng != null)
                GeoLocation(payload.lat, payload.lng)
            else null
        Log.d("ResQLink_Apply", "📍 [좌표수신] Lat: ${payload.lat}, Lng: ${payload.lng}")
        // Radar 진입 여부와 무관하게, 내 위치 + payload 위치 있으면 GPS 거리 계산
        val myLoc = locationProvider.getCurrentLocation() ?: store.getMyLocation()
        Log.d("ResQLink_Distance", "📍 내 위치: $myLoc, 상대 위치: $payloadLoc, 모드: ${store.mode.value}")

        // GPS 둘 다 있으면 (실제거리, RSSI) 로깅 → 학습/검증 데이터 수집
        if (payloadLoc != null && myLoc != null && rssiDbm != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                myLoc.lat, myLoc.lng,
                payloadLoc.lat, payloadLoc.lng,
                results
            )
            rssiDistanceLogger?.log(
                actualDistanceM = results[0].toDouble(),
                rssiDbm = rssiDbm,
                timestampMs = envelope.timestampMs
            )
        }

        store.onIncomingSos(
            originId = originId,
            msgId = envelope.msgId,
            rssiDbm = rssiDbm,
            payloadLocation = payloadLoc,
            myLocation = myLoc
        )

        // Inbox / UI 용 이벤트 방출
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
                hops = envelope.hops,
                myLocation = myLoc
            )
        )
    }

    private fun extractOriginId(envelope: MessageEnvelope): String {
        val firstHopFrom = envelope.hops.firstOrNull()?.from
        return firstHopFrom ?: envelope.senderId
    }
}