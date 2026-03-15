package com.example.resqlink.domain.gateway

/**
 * GPS 기반 실제 거리와 RSSI를 로컬에 기록하여
 * 나중에 학습/검증 데이터로 활용하기 위한 인터페이스.
 */
interface RssiDistanceLogger {
    /**
     * actualDistanceM: GPS로 계산한 실제 거리(미터)
     * rssiDbm: 해당 시점의 수신 신호 강도
     * timestampMs: 기록 시각
     */
    fun log(actualDistanceM: Double, rssiDbm: Int, timestampMs: Long)
}
