package com.example.resqlink.rag


import android.util.Log
import com.example.resqlink.rag.database.DataPackLoader
import com.example.resqlink.rag.database.RagChunk
import kotlin.math.sqrt

class RetrievalManager(
    private val dataPackLoader: DataPackLoader,
    private val embeddingHelper: EmbeddingHelper
) {

    suspend fun retrieve(query: String, topK: Int = 5): List<RagChunk> {
        if (!dataPackLoader.isLoaded) {
            Log.w("RetrievalManager", "데이터팩이 로드되지 않았습니다.")
            return emptyList()
        }

        val queryWithPrefix = "query: $query"
        // 1. 질문(Query) 임베딩
        val queryVector = embeddingHelper.embed(queryWithPrefix) ?: return emptyList()

        // 2. 코사인 유사도 검색 (Brute-force)
        val docEmbeddings = dataPackLoader.embeddings ?: return emptyList()
        val scores = ArrayList<Pair<Int, Float>>()

        for (i in docEmbeddings.indices) {
            val score = cosineSimilarity(queryVector, docEmbeddings[i])
            scores.add(i to score)
        }

        // 3. 넓은 후보 풀 확보 후 인접 청크 이웃 확장
        val candidates = scores
            .filter { it.second >= 0.3f }
            .sortedByDescending { it.second }
            .take(topK * 4)

        // 인접 이웃 청크도 후보에 추가
        val expandedIndices = mutableSetOf<Int>()
        for ((idx, _) in candidates) {
            expandedIndices.add(idx)
            val chunk = dataPackLoader.chunks[idx]
            val chunkNum = chunk.chunkId.substringAfterLast("_c").toIntOrNull() ?: continue
            // 앞뒤 청크 탐색
            for (neighborNum in listOf(chunkNum - 1, chunkNum + 1)) {
                if (neighborNum < 0) continue
                val neighborId = chunk.chunkId.substringBeforeLast("_c") + "_c$neighborNum"
                val neighborIdx = dataPackLoader.chunks.indexOfFirst {
                    it.docId == chunk.docId && it.chunkId == neighborId
                }
                if (neighborIdx >= 0) expandedIndices.add(neighborIdx)
            }
        }

        // 확장된 후보를 점수순 정렬
        val scoreMap = scores.associate { it.first to it.second }
        val expandedCandidates = expandedIndices
            .map { idx -> idx to (scoreMap[idx] ?: 0f) }
            .sortedByDescending { it.second }

        // 4. 인접 청크를 병합하여 상위 K개 그룹 추출
        return mergeAdjacentChunks(expandedCandidates, topK)
    }

    /**
     * 같은 문서의 인접 청크를 병합하여 하나의 완전한 텍스트로 합침.
     * 인접하지 않은 청크는 개별 항목으로 유지.
     */
    private fun mergeAdjacentChunks(
        candidates: List<Pair<Int, Float>>,
        topK: Int
    ): List<RagChunk> {
        data class ChunkGroup(
            val indices: MutableList<Int>,
            val maxScore: Float,
            val docId: String
        )

        val groups = mutableListOf<ChunkGroup>()

        for ((idx, score) in candidates) {
            val chunk = dataPackLoader.chunks[idx]
            val chunkNum = chunk.chunkId.substringAfterLast("_c").toIntOrNull()

            // 기존 그룹 중 같은 문서이면서 인접한 그룹 탐색
            val matchingGroup = if (chunkNum != null) {
                groups.find { group ->
                    group.docId == chunk.docId && group.indices.any { gIdx ->
                        areAdjacentChunks(dataPackLoader.chunks[gIdx].chunkId, chunk.chunkId)
                    }
                }
            } else null

            if (matchingGroup != null) {
                matchingGroup.indices.add(idx)
            } else {
                groups.add(ChunkGroup(mutableListOf(idx), score, chunk.docId))
            }
        }

        // 그룹을 최고 점수순 정렬 후 상위 K개 선택
        val topGroups = groups
            .sortedByDescending { it.maxScore }
            .take(topK)

        return topGroups.map { group ->
            // 그룹 내 청크를 chunkId 순으로 정렬하여 병합
            val sortedIndices = group.indices.sortedBy { idx ->
                dataPackLoader.chunks[idx].chunkId.substringAfterLast("_c").toIntOrNull() ?: 0
            }
            val baseChunk = dataPackLoader.chunks[sortedIndices.first()]
            val mergedContent = sortedIndices.joinToString("\n") { idx ->
                dataPackLoader.chunks[idx].content
            }

            Log.d("RetrievalManager", "Merged group: docId=${group.docId}, chunks=${sortedIndices.size}, score=${group.maxScore}, title=${baseChunk.docTitle}")

            baseChunk.copy(content = mergedContent)
        }
    }

    private fun areAdjacentChunks(id1: String, id2: String): Boolean {
        val n1 = id1.substringAfterLast("_c").toIntOrNull() ?: return false
        val n2 = id2.substringAfterLast("_c").toIntOrNull() ?: return false
        return kotlin.math.abs(n1 - n2) <= 1
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        return if (normA > 0 && normB > 0) {
            dotProduct / (sqrt(normA) * sqrt(normB))
        } else {
            0.0f
        }
    }
}