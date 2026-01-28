package com.example.resqlink.domain.model.Range

/**
 * RSSI 기반 거리 추정 결과
 * - distanceM: 추정 거리(미터). null이면 추정 불가
 * - bucket: UI에 쓸 거리 구간
 * - confidence: 0.0~1.0 (RSSI 튐이 적을수록 높음) -> 투명도 등에 활용 가능
 * - displayRange: "약 20~60m" 같은 표시용 범위 (선택)
 */
data class RangeEstimate(
    val distanceM: Double?,
    val bucket: RangeBucket,
    val confidence: Double,
    val displayRange: String?
)
