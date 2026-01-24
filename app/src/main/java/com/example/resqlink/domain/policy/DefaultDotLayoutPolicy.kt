package com.example.resqlink.domain.policy

import kotlin.math.max
import kotlin.math.min

class DefaultDotLayoutPolicy: DotLayoutPolicy {
    override fun angleDeg(originId: String): Float {
        // 안정적 랜덤: originId 해시 -> 0..359
        val h = originId.hashCode()
        val normalized = (h % 360 + 360) % 360
        return normalized.toFloat()
    }

    override fun alphaFromAgeSec(ageSec: Int): Float {
        // 0초=1.0, 5분=0.2로 선형 페이드(원하면 곡선으로 바꿔도 됨)
        val maxSec = 5 * 60
        val t = min(1f, ageSec.toFloat() / maxSec.toFloat())
        return max(0.2f, 1.0f - 0.8f * t)
    }
}