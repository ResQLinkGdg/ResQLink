package com.example.resqlink.rag.generation

import android.content.Context
import android.util.Log
import com.example.resqlink.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InferenceModel(private val context: Context) {

    // 🟢 Gemini 모델 설정
    // flash 모델이 빠르고 저렴하며 RAG 답변용으로 충분합니다.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY, // build.gradle에서 설정한 키 사용
        generationConfig = generationConfig {
            temperature = 0.5f // 답변의 창의성 조절 (0.0 ~ 1.0)
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    // 더 이상 무거운 모델 초기화(파일 복사 등)가 필요 없습니다.
    suspend fun initialize() {
        Log.d("InferenceModel", "Gemini API Client Ready")
    }

    suspend fun generateResponse(prompt: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("InferenceModel", "Requesting Gemini API...")

                // API 호출
                val response = generativeModel.generateContent(prompt)

                val answer = response.text
                Log.d("InferenceModel", "Gemini Response received: ${answer?.take(50)}...")

                answer
            } catch (e: Exception) {
                Log.e("InferenceModel", "Gemini API Error: ${e.message}", e)
                "죄송합니다. AI 응답을 생성하는 중 오류가 발생했습니다. (네트워크 상태를 확인해주세요)"
            }
        }
    }

    fun close() {
        // API 클라이언트는 특별한 해제 작업이 필요 없습니다.
    }
}

//package com.example.resqlink.rag.generation
//
//import android.content.Context
//import android.util.Log
//import com.google.mediapipe.tasks.genai.llminference.LlmInference
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//
//class InferenceModel(private val context: Context) {
//
//    private var llmInference: LlmInference? = null
//    private val modelFileName = "gemma3-1B-it-int4.tflite"
//
//    suspend fun initialize() = withContext(Dispatchers.IO) {
//        if (llmInference != null) return@withContext
//
//        try {
//            Log.d("InferenceModel", "모델 초기화 시작...")
//
//            val modelFile = File(context.filesDir, modelFileName)
//            if (!modelFile.exists()) {
//                Log.d("InferenceModel", "모델 파일 복사 중...")
//                context.assets.open(modelFileName).use { inputStream ->
//                    modelFile.outputStream().use { outputStream ->
//                        inputStream.copyTo(outputStream)
//                    }
//                }
//            }
//
//            val options = LlmInference.LlmInferenceOptions.builder()
//                .setModelPath(modelFile.absolutePath)
//                .setMaxTokens(1024) // 생성할 최대 토큰 수
//                .setTemperature(0.7f)
//                .setTopK(40)
//                .build()
//
//            llmInference = LlmInference.createFromOptions(context, options)
//            Log.d("InferenceModel", "온디바이스 모델 로드 완료!")
//
//        } catch (e: Exception) {
//            Log.e("InferenceModel", "모델 로드 실패", e)
//            // 에러가 나도 앱이 죽지 않도록 예외 처리
//        }
//    }
//
//    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
//        try {
//            if (llmInference == null) {
//                return@withContext "모델이 아직 초기화되지 않았습니다. 잠시만 기다려주세요."
//            }
//
//            // Gemma 3 프롬프트 포맷
//            val formattedPrompt = """
//                <start_of_turn>user
//                $prompt<end_of_turn>
//                <start_of_turn>model
//            """.trimIndent()
//
//            // 추론 실행
//            llmInference?.generateResponse(formattedPrompt) ?: "답변 생성 실패"
//        } catch (e: Exception) {
//            Log.e("InferenceModel", "추론 중 에러 발생", e)
//            "에러 발생: ${e.message}"
//        }
//    }
//
//    // 메모리 해제가 필요할 때 호출
//    fun close() {
//        llmInference = null
//    }
//}