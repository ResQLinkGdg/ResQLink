package com.example.resqlink.domain.usecase.reach

import com.example.resqlink.domain.gateway.LocationProvider
import com.example.resqlink.domain.gateway.Transport
import com.example.resqlink.platform.reach.protocol.MessageFactory
import com.example.resqlink.platform.reach.protocol.MessageCodec

class ReachControlUseCase(
    private val transport: Transport,
    private val locationProvider: LocationProvider,
    private val codec: MessageCodec,
    private val mySenderId: String
) {

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
    suspend fun sendSos(ttl: Int, text: String?) {
        val loc = locationProvider.getCurrentLocation()

        val sos = MessageFactory.newSos(
            senderId = mySenderId,
            ttl = ttl,
            lat = loc?.lat,
            lng = loc?.lng,
            text = text
        )

        transport.broadcast(codec.encode(sos))
    }

}
