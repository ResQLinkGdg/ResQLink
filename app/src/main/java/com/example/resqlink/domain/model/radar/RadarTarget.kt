package com.example.resqlink.domain.model.radar

import com.example.resqlink.domain.model.Range.RangeBucket

data class RadarTarget(
    val key: String,
    val lastSeenMs: Long,
    val bucket: RangeBucket,
    val approxRangeText: String?,
    val bearingDeg: Double?,    // GPS ON에서만 의미
    val hasGps: Boolean
)
