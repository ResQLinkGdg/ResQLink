package com.example.resqlink.domain.usecase

import com.example.resqlink.domain.gateway.DistributionHistoryStore
import com.example.resqlink.domain.model.distribution.DistributionPoint
class GetDistributionHistoryUseCase(
    private val history: DistributionHistoryStore
) {
    fun execute(now: Long, minutes: Int = 30): List<DistributionPoint> {
        val since = now - minutes * 60_000L
        return history.getSeries(sinceTime = since, now = now)
    }
}