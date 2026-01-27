package com.example.resqlink.domain.model.distribution

import com.example.resqlink.domain.model.ProximitySource
import com.example.resqlink.domain.model.proximity.ProximityBucket
import com.example.resqlink.domain.model.proximity.ProximityConfidence

data class RadarDot(
    val originId: String,
    val bucket: ProximityBucket,
    val angleDeg: Float,
    val lastSeenAt: Long,
    val confidence: ProximityConfidence,
    val source: ProximitySource,
)