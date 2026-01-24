package com.example.resqlink.domain.model

data class RadarDot(
    val originId: String,
    val bucket: ProximityBucket,
    val angleDeg: Float,
    val lastSeenAt: Long,
    val confidence: ProximityConfidence,
    val source: ProximitySource,
)
