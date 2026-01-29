package com.example.resqlink.domain.model.sos

import com.example.resqlink.domain.gateway.GeoLocation
import com.example.resqlink.platform.reach.protocol.HopSignal
import com.example.resqlink.platform.reach.protocol.sos.SosSituation
import com.example.resqlink.platform.reach.protocol.sos.SosUrgency

/**
 * Domain event representing the fact that an SOS message has arrived.
 *
 * - Transport / UI independent
 * - Pure factual data
 */
data class IncomingSosEvent(
    val originId: String,          // 최초 송신자
    val msgId: String,             // 메시지 ID
    val urgency: SosUrgency,       // 긴급도
    val situation: SosSituation,   // 상황 유형
    val peopleCount: Int?,         // 인원 수 (optional)
    val hint: String?,             // 자유 설명
    val payloadLocation: GeoLocation?, // 송신자가 포함한 위치
    val rssiDbm: Int?,             // 마지막 수신 RSSI
    val timestampMs: Long,         // 수신 시각
    val hops: List<HopSignal>      // hop 정보 (전파 경로)
)
