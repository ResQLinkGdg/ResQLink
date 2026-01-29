package com.example.resqlink.rag.generation

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InferenceModel(private val context: Context) {

    private var llmInference: LlmInference? = null
    private val modelFileName = "gemma3-1B-it-int4.tflite"

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (llmInference != null) return@withContext

        try {
            Log.d("InferenceModel", "모델 초기화 시작...")

            val modelFile = File(context.filesDir, modelFileName)
            if (!modelFile.exists()) {
                Log.d("InferenceModel", "모델 파일 복사 중...")
                context.assets.open(modelFileName).use { inputStream ->
                    modelFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(1024) // 생성할 최대 토큰 수
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("InferenceModel", "온디바이스 모델 로드 완료!")

        } catch (e: Exception) {
            Log.e("InferenceModel", "모델 로드 실패", e)
            // 에러가 나도 앱이 죽지 않도록 예외 처리
        }
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            if (llmInference == null) {
                return@withContext "모델이 아직 초기화되지 않았습니다. 잠시만 기다려주세요."
            }

            // Gemma 3 프롬프트 포맷
            val formattedPrompt = """
                <start_of_turn>user
                $prompt<end_of_turn>
                <start_of_turn>model
            """.trimIndent()

            // 추론 실행
            llmInference?.generateResponse(formattedPrompt) ?: "답변 생성 실패"
        } catch (e: Exception) {
            Log.e("InferenceModel", "추론 중 에러 발생", e)
            "에러 발생: ${e.message}"
        }
    }

    // 메모리 해제가 필요할 때 호출
    fun close() {
        llmInference = null
    }
}