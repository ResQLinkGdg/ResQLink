package com.example.resqlink.rag

object SearchUtils {
    fun calculateCosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
            normA += vector1[i] * vector1[i]
            normB += vector2[i] * vector2[i]
        }
        val denominator = (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat()
        return if (denominator == 0f) 0f else dotProduct / denominator
    }
}