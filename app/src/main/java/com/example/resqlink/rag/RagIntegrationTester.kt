package com.example.resqlink.rag

// RagIntegrationTester.kt

import android.util.Log
import com.example.resqlink.rag.database.ManualDao // 본인 패키지명 확인
import com.example.resqlink.rag.database.ManualSearchManager
import com.example.resqlink.rag.database.sampleDataPack
import com.example.resqlink.rag.generation.GenAiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RagIntegrationTester(
    private val dao: ManualDao,
    private val embeddingHelper: EmbeddingHelper,
    private val searchManager: ManualSearchManager,
    private val genAiManager: GenAiManager
) {
    suspend fun runFullTest() {
        withContext(Dispatchers.IO) { // DB와 AI 작업은 IO 스레드에서
            Log.d("RAG_TEST", "=== 1단계: 데이터팩 초기화 시작 ===")

            // 1. 기존 데이터가 있다면 충돌 방지를 위해 설명용으로 유지하거나,
            // 실제 테스트에선 보통 비우고 시작하지만 여기선 Insert만 합니다.

            sampleDataPack.forEach { manual ->
                // 제목, 키워드, 본문을 합쳐서 임베딩 생성
                val textToEmbed = "제목: ${manual.title} \n 키워드: ${manual.keywords} \n 내용: ${manual.content}"

                // 벡터 생성 (이 과정이 없으면 검색이 안 됩니다!)
                val vector = embeddingHelper.getEmbedding(textToEmbed)

                if (vector != null) {
                    val manualWithVector = manual.copy(embedding = vector)
                    dao.insertManual(manualWithVector) // DB 저장
                    Log.d("RAG_TEST", "[저장완료] ${manual.title}")
                } else {
                    Log.e("RAG_TEST", "[실패] 임베딩 생성 실패: ${manual.title}")
                }
            }
            Log.d("RAG_TEST", "=== 1단계 완료: 데이터 준비 끝 ===")


            // 2. 가상 질문 던지기
            val testQuery = "배터리가 너무 빨리 닳는데 어떻게 해야 해?"
            Log.d("RAG_TEST", "=== 2단계: 검색 시작 (질문: $testQuery) ===")

            // 유사도 검색 실행 (상위 1개만)
            val searchResults = searchManager.searchTopK(testQuery, k = 1)

            if (searchResults.isNotEmpty()) {
                val foundManual = searchResults[0]
                Log.d("RAG_TEST", "[검색성공] 찾은 매뉴얼: ${foundManual.title}")
                Log.d("RAG_TEST", "[유사도 내용] ${foundManual.content}")

                // 3. LLM 답변 생성
                Log.d("RAG_TEST", "=== 3단계: LLM 답변 생성 요청 ===")

                val prompt = """
                    당신은 기술 지원 봇입니다. 아래 [정보]를 보고 [질문]에 답하세요.
                    
                    [정보]
                    ${foundManual.content}
                    
                    [질문]
                    $testQuery
                    
                    답변:
                """.trimIndent()

                // 실제 답변 생성 (Gemini Nano 또는 Gemma 연결된 상태여야 함)
                val finalAnswer = genAiManager.generateResponse(prompt)

                Log.d("RAG_TEST", "====================================")
                Log.d("RAG_TEST", "🤖 최종 AI 답변: $finalAnswer")
                Log.d("RAG_TEST", "====================================")
            } else {
                Log.e("RAG_TEST", "[검색실패] 관련 매뉴얼을 찾지 못했습니다.")
            }
        }
    }
}