package com.example.resqlink.domain.gateway

import com.example.resqlink.domain.model.NodeSummary
import com.example.resqlink.domain.model.SeenResult
class InMemoryMeshStore(
    private val dedupWindowMillis: Long = 10 * 60_000L, // 10분
): MeshStore {
    private val lock = Any()

    // msgId -> seenAt
    private val dedupMap: LinkedHashMap<String, Long> = LinkedHashMap()

    // originId -> record
    private val nodeMap: MutableMap<String, NodeRecord> = HashMap()

    override fun markSeen(msgId: String, now: Long): SeenResult {
        synchronized(lock) {
            pruneDedup(now)

            val prev = dedupMap[msgId]
            return if (prev != null && (now - prev) <= dedupWindowMillis) {
                SeenResult.DUPLICATE
            } else {
                dedupMap[msgId] = now
                SeenResult.NEW
            }
        }
    }

    override fun upsertNodeSeen(originId: String, now: Long, hopFromResponder: Int?) {
        synchronized(lock) {
            val rec = nodeMap[originId]
            if (rec == null) {
                nodeMap[originId] = NodeRecord(
                    originId = originId,
                    lastSeenAt = now,
                    minHopFromResponder = hopFromResponder
                )
            } else {
                rec.lastSeenAt = now
                // hop은 "최소 hop"을 유지하는 게 보통 더 의미있음
                rec.minHopFromResponder = minHop(rec.minHopFromResponder, hopFromResponder)
            }
        }
    }

    override fun listLiveNodes(sinceTime: Long, now: Long): List<NodeSummary> {
        synchronized(lock) {
            // sinceTime 이후로 살아있는 노드들만
            val live = nodeMap.values
                .asSequence()
                .filter { it.lastSeenAt >= sinceTime && it.lastSeenAt <= now }
                .toList()

            val clusterSize = live.size

            return live
                .sortedByDescending { it.lastSeenAt }
                .map {
                    NodeSummary(
                        originId = it.originId,
                        lastSeenAt = it.lastSeenAt,
                        minHopFromResponder = it.minHopFromResponder,
                        clusterSizeEstimate = clusterSize
                    )
                }
        }
    }

    private fun pruneDedup(now: Long) {
        val threshold = now - dedupWindowMillis
        val it = dedupMap.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.value < threshold) it.remove()
            else break // LinkedHashMap: 오래된 것부터 들어왔다고 가정하면 여기서 끊어도 됨
        }
    }

    private fun minHop(a: Int?, b: Int?): Int? {
        return when {
            a == null -> b
            b == null -> a
            else -> kotlin.math.min(a, b)
        }
    }

    private data class NodeRecord(
        val originId: String,
        var lastSeenAt: Long,
        var minHopFromResponder: Int?
    )

}