package com.example.resqlink.rag.generation

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GenAiManager(private val context: Context) {

    private var llmInference: LlmInference? = null

    private val modelFileName = "gemma-3-1b-it-gpu-int4.tflite"

    init {
        initializeModel()
    }

    private fun initializeModel() {
        try {
            val modelFile = File(context.filesDir, modelFileName)

            if (!modelFile.exists()) {
                Log.d("GenAiManager", "모델 파일 복사 중...")
                context.assets.open(modelFileName).use { inputStream ->
                    modelFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val options = LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)

            Log.d("GenAiManager", "Gemma 모델 로드 성공!")

        } catch (e: Exception) {
            Log.e("GenAiManager", "모델 초기화 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    // 이제 이 함수에서도 llmInference를 알아볼 수 있습니다.
    suspend fun generateResponse(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (llmInference == null) {
                    return@withContext "오류: 모델이 아직 로드되지 않았습니다."
                }

                // Gemma 3 프롬프트 형식 적용 (채팅 형식)
                val formattedPrompt = """
                    <start_of_turn>user
                    $prompt<end_of_turn>
                    <start_of_turn>model
                """.trimIndent()

                val result = llmInference?.generateResponse(formattedPrompt)
                result ?: "답변을 생성할 수 없습니다."

            } catch (e: Exception) {
                Log.e("GenAiManager", "추론 중 에러 발생: ${e.message}")
                "에러 발생: ${e.message}"
            }
        }
    }
}