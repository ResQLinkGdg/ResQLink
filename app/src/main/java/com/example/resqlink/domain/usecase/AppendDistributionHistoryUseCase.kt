package com.example.resqlink.domain.usecase

import com.example.resqlink.domain.gateway.DistributionHistoryStore
import com.example.resqlink.domain.model.proximity.ProximityBucket
import com.example.resqlink.domain.model.distribution.DistributionPoint
import com.example.resqlink.domain.model.distribution.DistributionSnapshot

class AppendDistributionHistoryUseCase (
    private val history: DistributionHistoryStore
) {
    fun execute(snapshot: DistributionSnapshot) {
        val ringsMap: Map<ProximityBucket, Int> =
            ProximityBucket.entries.associateWith { bucket ->
                snapshot.countOf(bucket)
            }

        history.append(
            DistributionPoint(
                at = snapshot.updatedAt,
                ringsByBucket = ringsMap
            )
        )
    }
}