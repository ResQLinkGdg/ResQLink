package com.example.resqlink.domain.usecase.reach

import android.util.Log
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
    private val mySenderId: String,
    private val transport: Transport,
    private val locationProvider: LocationProvider,
    private val codec: MessageCodec,
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
        // 1. 위치 획득 확인 로그
        val loc = if (includeLocation) {
            val currentLoc = locationProvider.getCurrentLocation()
            Log.d("ResQLink_SOS", "📍 위치 정보 가져옴: lat=${currentLoc?.lat}, lng=${currentLoc?.lng}")
            currentLoc
        } else {
            Log.d("ResQLink_SOS", "📍 위치 포함 안 함 (includeLocation=false)")
            null
        }

        // 2. SOS 객체 생성 확인 로그
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
        Log.d("ResQLink_SOS", "📦 SOS 객체 생성 완료: senderId=$mySenderId, urgency=$urgency, situation=$situation")

        // 3. 인코딩 및 전송 직전 로그
        val encodedData = codec.encode(sos)
        Log.d("ResQLink_SOS", "📡 전송 시작 (데이터 크기: ${encodedData.size} bytes)")

        try {
            transport.broadcast(encodedData)
            Log.d("ResQLink_SOS", "✅ 브로드캐스트 명령어 전달 성공!")
        } catch (e: Exception) {
            Log.e("ResQLink_SOS", "❌ 전송 중 에러 발생: ${e.message}")
        }
    }


}
