package com.example.resqlink.domain.model.distribution

import com.example.resqlink.domain.model.ProximityBucket
import com.example.resqlink.domain.model.RadarDot
import com.example.resqlink.domain.model.RingGroup

data class DistributionSnapshot(
    val updatedAt: Long,
    val windowMinutes: Int,
    val rings: List<RingGroup>,   // 항상 4개
    val dots: List<RadarDot>
) {
    fun countOf(bucket: ProximityBucket): Int =
        rings.firstOrNull { it.bucket == bucket }?.count ?: 0
}