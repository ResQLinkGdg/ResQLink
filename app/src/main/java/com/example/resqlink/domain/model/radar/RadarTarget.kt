package com.example.resqlink.domain.model.radar

import com.example.resqlink.domain.model.Range.RangeBucket

data class RadarTarget(
    val key: String,
    val lastSeenMs: Long,
    val bucket: RangeBucket,
    val approxRangeText: String?,
    val bearingDeg: Double?,    // GPS ON에서만 의미
<<<<<<< HEAD
    val hasGps: Boolean,
    val distanceM: Double?
=======
    val hasGps: Boolean
>>>>>>> c3c7fa588f6255b2cb07249899b5fd067c0b13e4
)
