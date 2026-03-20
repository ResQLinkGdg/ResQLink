package com.example.resqlink.rag.generation

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InferenceModel(private val context: Context) {

    private var llmInference: LlmInference? = null
    private val modelFileName = "gemma3-1b-it-int4.task"

    var isReady = false
        private set
    var errorMessage: String? = null
        private set

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (llmInference != null) return@withContext

        try {
            Log.d("InferenceModel", "모델 초기화 시작...")

            val modelFile = File(context.filesDir, modelFileName)
            if (!modelFile.exists()) {
                Log.d("InferenceModel", "모델 파일 복사 중... (529MB)")
                context.assets.open(modelFileName).use { inputStream ->
                    modelFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("InferenceModel", "모델 파일 복사 완료")
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(1024)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isReady = true
            Log.d("InferenceModel", "온디바이스 모델 로드 완료!")

        } catch (e: Exception) {
            errorMessage = e.message
            Log.e("InferenceModel", "모델 로드 실패", e)
        }
    }


    suspend fun generateResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            if (llmInference == null) {
                return@withContext "모델이 아직 초기화되지 않았습니다. 잠시만 기다려주세요."
            }

            val formattedPrompt = """
                <start_of_turn>user
                $prompt<end_of_turn>
                <start_of_turn>model
            """.trimIndent()

            llmInference?.generateResponse(formattedPrompt) ?: "답변 생성 실패"
        } catch (e: Exception) {
            Log.e("InferenceModel", "추론 중 에러 발생", e)
            "에러 발생: ${e.message}"
        }
    }

    fun close() {
        llmInference = null
    }
}
