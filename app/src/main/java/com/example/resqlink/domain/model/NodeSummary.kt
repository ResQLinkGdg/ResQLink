package com.example.resqlink.domain.model
/**
 * UI/Policy에 필요한 최소 요약
 * - minHopFromResponder: 작을수록 responder가 접근 가능한(가까운) 경향
 * - clusterSizeEstimate: 최근 window에서 관측된 유니크 origin 수(전역 카운트)
 */
data class NodeSummary(
    val originId: String,
    val lastSeenAt: Long,
    val minHopFromResponder: Int? = null,
    val clusterSizeEstimate: Int = 0
)
