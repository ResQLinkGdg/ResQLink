package com.example.resqlink.domain.model.distribution

import com.example.resqlink.domain.model.ProximityBucket

data class DistributionPoint(
    val at: Long,
    val ringsByBucket: Map<ProximityBucket, Int> // bucket -> count
)