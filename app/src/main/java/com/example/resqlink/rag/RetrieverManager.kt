package com.example.resqlink.rag


import android.util.Log
import com.example.resqlink.rag.database.DataPackLoader
import com.example.resqlink.rag.database.RagChunk
import kotlin.math.sqrt

class RetrievalManager(
    private val dataPackLoader: DataPackLoader,
    private val embeddingHelper: EmbeddingHelper
) {

    suspend fun retrieve(query: String, topK: Int = 3): List<RagChunk> {
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

        // 3. 상위 K개 추출
        val topIndices = scores.sortedByDescending { it.second }.take(topK)

        return topIndices.map { (index, score) ->
            Log.d("RetrievalManager", "Found: idx=$index, score=$score, title=${dataPackLoader.chunks[index].docTitle}")
            dataPackLoader.chunks[index]
        }
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