package com.example.resqlink.domain.policy

import com.example.resqlink.domain.model.NodeSummary
import com.example.resqlink.domain.model.ProximityBucket
import com.example.resqlink.domain.model.ProximityConfidence
import com.example.resqlink.domain.model.ProximityHint
import com.example.resqlink.domain.model.ProximitySource
import kotlin.math.max

/**
 * GRAPH 기반 버킷 추정:
 * - 핵심: 절대거리(m)가 아니라 "접근 용이성"을 링으로 표현
 * - 기본은 hop(minHopFromResponder) 기반
 * - hop이 없으면 UNKNOWN (추정 근거가 약하니)
 * - confidence는 hop + 최근성(age) + 관측수(cluster)로 조정
 */
class GraphRangingPolicy(
    // hop 기준 링 구분 (MVP: 직관적인 룰)
    private val nearHopMax: Int = 1,
    private val midHopMax: Int = 3,

    // 최근성(초) 기준: 신호가 너무 오래되면 confidence 낮추기
    private val freshSec: Int = 30,
    private val staleSec: Int = 120
):RangingPolicy {
    override fun hintFor(node: NodeSummary, now: Long): ProximityHint {
        val ageSec = max(0, ((now - node.lastSeenAt) / 1000L).toInt())
        val hop = node.minHopFromResponder

        val bucket = when {
            hop == null -> ProximityBucket.UNKNOWN
            hop <= nearHopMax -> ProximityBucket.NEAR
            hop <= midHopMax -> ProximityBucket.MID
            else -> ProximityBucket.FAR
        }

        val confidence = computeConfidence(
            hop = hop,
            ageSec = ageSec,
            clusterSize = node.clusterSizeEstimate
        )

        return ProximityHint(
            distanceMeters = null,                 // GRAPH는 절대거리 제공 X
            bucket = bucket,
            confidence = confidence,
            source = ProximitySource.GRAPH
        )
    }

    private fun computeConfidence(hop: Int?, ageSec: Int, clusterSize: Int): ProximityConfidence {
        // hop이 없으면 근거 부족
        if (hop == null) return ProximityConfidence.LOW

        // 기본: hop이 있으면 최소 MEDIUM까지는 가능
        var base = when {
            hop <= 1 -> ProximityConfidence.MEDIUM
            hop <= 3 -> ProximityConfidence.LOW
            else -> ProximityConfidence.LOW
        }

        // 신호가 아주 신선하면 한 단계 업 (단, hop이 near 쪽일 때)
        if (ageSec <= freshSec && hop <= 1) {
            base = ProximityConfidence.HIGH
        } else if (ageSec <= staleSec && hop <= 2 && base == ProximityConfidence.LOW) {
            base = ProximityConfidence.MEDIUM
        }

        // clusterSize가 크면(여러 origin이 잡히는 환경) 추정 안정성 약간 상승
        if (clusterSize >= 8 && base == ProximityConfidence.LOW) {
            base = ProximityConfidence.MEDIUM
        }

        // 너무 오래되면 하향
        if (ageSec > staleSec) {
            base = ProximityConfidence.LOW
        }

        return base
    }
}