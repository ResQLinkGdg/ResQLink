package com.example.resqlink.domain.usecase

import com.example.resqlink.domain.gateway.MeshStore
import com.example.resqlink.domain.model.ProximityBucket
import com.example.resqlink.domain.model.RadarDot
import com.example.resqlink.domain.model.RingGroup
import com.example.resqlink.domain.model.distribution.DistributionSnapshot
import com.example.resqlink.domain.policy.DotLayoutPolicy
import com.example.resqlink.domain.policy.RangingPolicy

class GetDistributionSnapshotUseCase(
    private val meshStore: MeshStore,
    private val rangingPolicy: RangingPolicy,
    private val dotLayoutPolicy: DotLayoutPolicy,
) {
    fun execute(now: Long, windowMinutes: Int = 5): DistributionSnapshot {
        val since = now - windowMinutes * 60_000L

        // 1) 최근 window 안에 살아있는 노드들 가져오기
        val nodes = meshStore.listLiveNodes(sinceTime = since, now = now)

        // 2) 각 노드를 RadarDot으로 변환
        val dots = nodes.map { node ->
            val hint = rangingPolicy.hintFor(node = node, now = now)

            RadarDot(
                originId = node.originId,
                bucket = hint.bucket,
                angleDeg = dotLayoutPolicy.angleDeg(node.originId),
                lastSeenAt = node.lastSeenAt,           // ✅ 최근성 원천
                confidence = hint.confidence,           // ✅ 근거(신뢰도)
                source = hint.source                    // ✅ 근거(출처)
            )
        }

        // 3) 링별 카운트 집계 (항상 4개 버킷 생성)
        val rings = ProximityBucket.entries.map { bucket ->
            RingGroup(
                bucket = bucket,
                count = dots.count { it.bucket == bucket }
            )
        }

        // 4) 스냅샷 반환
        return DistributionSnapshot(
            updatedAt = now,
            windowMinutes = windowMinutes,
            rings = rings,
            dots = dots
        )
    }
}