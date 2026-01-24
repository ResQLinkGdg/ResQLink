package com.example.resqlink.domain.model

data class ProximityHint(
    val distanceMeters: Float? = null,
    val bucket: ProximityBucket = ProximityBucket.UNKNOWN,
    val confidence: ProximityConfidence = ProximityConfidence.LOW,
    val source: ProximitySource = ProximitySource.GRAPH
)
