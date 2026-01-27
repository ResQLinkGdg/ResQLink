package com.example.resqlink.data.store

import com.example.resqlink.data.room.dao.DistributionHistoryDao
import com.example.resqlink.data.room.entity.DistributionPointEntity
import com.example.resqlink.domain.gateway.DistributionHistoryStore
import com.example.resqlink.domain.model.distribution.DistributionPoint
import kotlin.collections.map

class RoomDistributionHistoryStore(
    private val dao: DistributionHistoryDao
) : DistributionHistoryStore {

    override fun append(point: DistributionPoint) {
        dao.insert(DistributionPointEntity.fromDomain(point))
    }

    override fun getSeries(
        sinceTime: Long,
        now: Long
    ): List<DistributionPoint> {
        return dao.getBetween(sinceTime, now).map { it.toDomain() }
    }

    override fun prune(
        keepSinceTime: Long,
        now: Long
    ) {
        dao.deleteBefore(keepSinceTime)
    }
}
