package com.example.resqlink.rag

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmbeddingHelper(private val context: Context) {

    private var textEmbedder: TextEmbedder? = null
    // 주의: assets 폴더에 이 이름의 모델 파일이 있어야 합니다.
    private val modelFileName = "intfloat_multilingual-e5-small.tflite"

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelFileName)
                .build()

            val options = TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .build()

            textEmbedder = TextEmbedder.createFromOptions(context, options)
            Log.d("EmbeddingHelper", "TextEmbedder initialized.")
        } catch (e: Exception) {
            Log.e("EmbeddingHelper", "Failed to init TextEmbedder: ${e.message}")
        }
    }

    suspend fun embed(text: String): FloatArray? = withContext(Dispatchers.IO) {
        try {
            val result = textEmbedder?.embed(text)
            // 첫 번째 임베딩 결과의 float 배열 반환
            result?.embeddingResult()?.embeddings()?.firstOrNull()?.floatEmbedding()
        } catch (e: Exception) {
            Log.e("EmbeddingHelper", "Embedding failed", e)
            null
        }
    }
}