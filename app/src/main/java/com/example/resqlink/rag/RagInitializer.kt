package com.example.resqlink.rag

import android.content.Context
import com.example.resqlink.rag.database.AppDatabase
import com.example.resqlink.rag.database.Manual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RagInitializer(private val context: Context, private val db: AppDatabase) {

    // 1단계에서 만든 임베딩 헬퍼 (MediaPipe 사용)
    private val embeddingHelper = EmbeddingHelper(context)

    suspend fun initializeDataPack(rawManuals: List<Manual>) {
        withContext(Dispatchers.Default) {
            rawManuals.forEach { manual ->
                // 제목, 키워드, 본문을 합쳐서 문맥을 풍부하게 만듭니다.
                val combinedText = "${manual.title} ${manual.keywords} ${manual.content}"

                // 텍스트를 벡터(FloatArray)로 변환
                val vector = embeddingHelper.getEmbedding(combinedText)

                // 벡터가 포함된 최종 객체를 생성하여 DB에 저장
                val manualWithEmbedding = manual.copy(embedding = vector)
                db.manualDao().insertManual(manualWithEmbedding)
            }
        }
    }
}