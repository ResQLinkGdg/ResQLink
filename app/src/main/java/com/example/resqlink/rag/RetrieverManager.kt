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

        // 3. 넓은 후보 풀에서 인접 청크 중복 제거 후 상위 K개 추출
        val candidates = scores
            .filter { it.second >= 0.3f }
            .sortedByDescending { it.second }
            .take(topK * 3)

        val selected = mutableListOf<Pair<Int, Float>>()
        for (candidate in candidates) {
            if (selected.size >= topK) break
            val chunk = dataPackLoader.chunks[candidate.first]
            val isDuplicate = selected.any { (idx, _) ->
                val sel = dataPackLoader.chunks[idx]
                sel.docId == chunk.docId && areAdjacentChunks(sel.chunkId, chunk.chunkId)
            }
            if (!isDuplicate) selected.add(candidate)
        }

        return selected.map { (index, score) ->
            Log.d("RetrievalManager", "Found: idx=$index, score=$score, title=${dataPackLoader.chunks[index].docTitle}")
            dataPackLoader.chunks[index]
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