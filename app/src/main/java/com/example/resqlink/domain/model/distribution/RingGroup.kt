package com.example.resqlink.domain.model.distribution

import com.example.resqlink.domain.model.proximity.ProximityBucket

data class RingGroup(
    val bucket: ProximityBucket,
    val count: Int,
)