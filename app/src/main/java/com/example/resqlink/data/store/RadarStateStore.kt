package com.example.resqlink.data.store

import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.domain.model.radar.RadarMode
import com.example.resqlink.domain.model.radar.RadarTarget
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory 상태 저장소(너 역할 핵심)
 * - 수신한 SOS를 여기에 넣으면 targets가 갱신되고 UI가 그린다.
 */
interface RadarStateStore {
    val mode: StateFlow<RadarMode>
    val targets: StateFlow<List<RadarTarget>>

    fun setMode(mode: RadarMode)

    fun onIncomingSos(
        originId: String,
        msgId: String,
        rssiDbm: Int?,
        payloadLocation: GeoLocation?,  // SOS payload lat/lng
        myLocation: GeoLocation?        // GPS ON일 때만 필요
<<<<<<< HEAD
    ){

    }
=======
    )
>>>>>>> c3c7fa588f6255b2cb07249899b5fd067c0b13e4

    fun onMyLocationUpdated(myLocation: GeoLocation)

}