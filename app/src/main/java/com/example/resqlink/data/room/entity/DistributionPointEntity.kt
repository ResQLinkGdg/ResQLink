package com.example.resqlink.data.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.resqlink.domain.model.distribution.DistributionPoint
import com.example.resqlink.domain.model.proximity.ProximityBucket

@Entity(
    tableName = "distribution_points",
    indices = [Index(value = ["observedAt"])]
)
data class DistributionPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val observedAt: Long,      // 스냅샷 시각(ms)
    val bucket: String,        // enum name 저장
    val count: Int,
    val windowMinutes: Int     // 어떤 window로 계산된 점인지(옵션이지만 넣어두면 안전)
)
{
    fun toDomain(): DistributionPoint =
        DistributionPoint(
            observedAt = observedAt,
            bucket = ProximityBucket.valueOf(bucket),
            count = count,
            windowMinutes = windowMinutes
        )

    companion object {
        fun fromDomain(p: DistributionPoint): DistributionPointEntity =
            DistributionPointEntity(
                observedAt = p.observedAt,
                bucket = p.bucket.name,
                count = p.count,
                windowMinutes = p.windowMinutes
            )
    }