package com.example.resqlink.domain.usecase.reach

import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.domain.model.sos.IncomingSosEvent
import com.example.resqlink.domain.usecase.radar.ApplyIncomingSosUsecase
import com.example.resqlink.platform.reach.protocol.MessageFactory
import com.example.resqlink.platform.reach.protocol.MessageCodec
import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency
import kotlinx.coroutines.flow.Flow

class ReachControlUseCase(
    private val transport: Transport,
    private val locationProvider: LocationProvider,
    private val codec: MessageCodec,
    private val mySenderId: String,
    private val applyIncomingSos: ApplyIncomingSosUsecase
) {

    val incomingSosFlow: Flow<IncomingSosEvent>
        get() = applyIncomingSos.incomingSosFlow

    /**
     * 재난 대응 모드 시작
     * - Nearby 광고 + 탐색 시작
     */
    fun startReachMode() {
        transport.startAdvertising()
        transport.startDiscovery()
    }

    /**
     * 재난 대응 모드 종료
     */
    fun stopReachMode() {
        transport.shutdown()
    }

    /**
     * SOS 최초 생성 & 전파
     */
    suspend fun sendSos(
        ttl: Int,
        urgency: SosUrgency,
        situation: SosSituation,
        peopleCount: Int?,
        hint: String?,
        includeLocation: Boolean
    ) {
        val loc = if (includeLocation) {
            locationProvider.getCurrentLocation()
        } else null

        val sos = MessageFactory.newSos(
            senderId = mySenderId,
            ttl = ttl,
            urgency = urgency,
            situation = situation,
            peopleCount = peopleCount,
            hint = hint,
            lat = loc?.lat,
            lng = loc?.lng
        )

        transport.broadcast(codec.encode(sos))
    }


}
