package com.example.resqlink.rag.database

import com.example.resqlink.rag.EmbeddingHelper
import com.example.resqlink.rag.SearchUtils

class ManualSearchManager(
    private val dao: ManualDao,
    private val embeddingHelper: EmbeddingHelper
) {
    suspend fun searchTopK(query: String, k: Int = 3): List<Manual> {
        // 1. 사용자 질문을 벡터로 변환 (1단계 활용)
        val queryVector = embeddingHelper.getEmbedding(query) ?: return emptyList()

        // 2. DB에서 모든 매뉴얼 가져오기
        val allManuals = dao.getAllManuals()

        // 3. 유사도 계산 및 점수 매기기
        val scoredList = allManuals.mapNotNull { manual ->
                // DB에 저장된 벡터가 null인 경우 건너뜁니다.
                val manualVector = manual.embedding ?: return@mapNotNull null

            // 질문 벡터와 DB 벡터 비교 (SearchUtils 활용)
            val score = SearchUtils.calculateCosineSimilarity(queryVector, manualVector)

            manual to score
        }

        // 4. 높은 점수 순으로 정렬 후 상위 k개만 추출
        return scoredList
                .sortedByDescending { it.second }
            .filter { it.second > 0.4f } // (옵션) 유사도가 너무 낮은 것은 제외
            .take(k)
                .map { it.first }
    }
}