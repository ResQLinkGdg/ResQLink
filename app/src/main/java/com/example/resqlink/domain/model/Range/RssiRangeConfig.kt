package com.example.resqlink.domain.model.Range

data class RssiRangeConfig(
    // 여기만 바꾸면 “거리 구간”이 바뀜
    val nearMaxM: Double = 50.0,
    val midMaxM: Double = 150.0,

    // RSSI->거리 모델 파라미터(튜닝 포인트)
    val txPowerAt1mDbm: Int = -59,
    val pathLossN: Double = 2.7,

    // 스무딩(EMA)
    val alpha: Double = 0.30,          // EMA
    val varAlpha: Double = 0.20,       // RSSI 튐(변화량) EMA

    //  결과 클램프/만료
    val minDistanceM: Double = 1.0,
    val maxDistanceM: Double = 250.0,
    val expireMs: Long = 2 * 60_000L,

    // confidence 계산 튜닝(값이 클수록 엄격)
    val varScaleDb: Double = 12.0,

    // UI용 “표시 범위” 배수(단정적 숫자 대신 범위로)
    val bandNear: RangeBand = RangeBand(0.6, 1.6),
    val bandMid: RangeBand  = RangeBand(0.7, 1.8),
    val bandFar: RangeBand  = RangeBand(0.8, 2.2),
)
