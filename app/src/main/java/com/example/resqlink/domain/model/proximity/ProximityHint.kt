package com.example.resqlink.domain.model.proximity

import com.example.resqlink.domain.model.proximity.ProximityConfidence
import com.example.resqlink.domain.model.ProximitySource

data class ProximityHint(
    val distanceMeters: Float? = null,
    val bucket: ProximityBucket = ProximityBucket.UNKNOWN,
    val confidence: ProximityConfidence = ProximityConfidence.LOW,
    val source: ProximitySource = ProximitySource.GRAPH
)