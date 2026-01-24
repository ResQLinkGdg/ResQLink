package com.example.resqlink.domain.gateway

import com.example.resqlink.domain.model.distribution.DistributionPoint

interface DistributionHistoryStore {
    fun append(point: DistributionPoint)
    fun getSeries(sinceTime: Long, now: Long = System.currentTimeMillis()): List<DistributionPoint>
    fun prune(keepSinceTime: Long, now: Long = System.currentTimeMillis())
}