package com.example.resqlink.rag

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions

class EmbeddingHelper(context: Context) {
    private var textEmbedder: TextEmbedder? = null

    init {
        // 1. 모델 설정 (assets 폴더에 모델 파일이 있어야 함)
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("Gecko_256_quant.tflite")
            .build()

        val options = TextEmbedderOptions.builder()
            .setBaseOptions(baseOptions)
            .build()

        // 2. 모델 로드
        textEmbedder = TextEmbedder.createFromOptions(context, options)
    }

    // 텍스트를 벡터로 바꾸는 함수
    fun getEmbedding(text: String): FloatArray? {
        return try {
            val result = textEmbedder?.embed(text)
            // 결과물에서 첫 번째 임베딩의 float 값을 가져옴
            result?.embeddingResult()?.embeddings()?.get(0)?.floatEmbedding()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        textEmbedder?.close()
    }
}