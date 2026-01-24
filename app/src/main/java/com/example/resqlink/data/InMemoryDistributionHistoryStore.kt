package com.example.resqlink.data

import com.example.resqlink.domain.gateway.DistributionHistoryStore
import com.example.resqlink.domain.model.distribution.DistributionPoint
import java.util.ArrayDeque

class InMemoryDistributionHistoryStore(
    keepMinutes: Int = 60,
    private val maxPoints: Int = 720 // 예: 5초마다 기록이면 60분=720개
) : DistributionHistoryStore {

    private val keepMillis: Long = keepMinutes * 60_000L
    private val deque: ArrayDeque<DistributionPoint> = ArrayDeque(maxPoints)
    private val lock = Any()

    override fun append(point: DistributionPoint) {
        synchronized(lock) {
            // 1) 새 포인트 추가
            deque.addLast(point)

            // 2) 용량 기반 링버퍼 컷 (최대 개수 유지)
            while (deque.size > maxPoints) {
                deque.removeFirst()
            }

            // 3) 시간 기반 컷 (keepMinutes보다 오래된 포인트 제거)
            val keepSinceTime = point.at - keepMillis
            pruneLocked(keepSinceTime)
        }
    }

    override fun getSeries(
        sinceTime: Long,
        now: Long
    ): List<DistributionPoint> {
        synchronized(lock) {
            if (deque.isEmpty()) return emptyList()

            val result = ArrayList<DistributionPoint>()
            for (p in deque) {
                if (p.at < sinceTime) continue
                if (p.at > now) break // 시간순 append 가정이면 break 가능
                result.add(p)
            }
            return result
        }
    }

    override fun prune(
        keepSinceTime: Long,
        now: Long
    ) {
        synchronized(lock) {
            pruneLocked(keepSinceTime)
        }
    }

    private fun pruneLocked(keepSinceTime: Long) {
        while (deque.isNotEmpty() && deque.first().at < keepSinceTime) {
            deque.removeFirst()
        }
    }
}
