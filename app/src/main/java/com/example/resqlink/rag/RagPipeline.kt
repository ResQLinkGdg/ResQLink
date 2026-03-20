package com.example.resqlink.rag


import com.example.resqlink.rag.database.RagChunk
import com.example.resqlink.rag.generation.InferenceModel

class RagPipeline(
    private val inferenceModel: InferenceModel,
    private val retrievalManager: RetrievalManager
) {
    suspend fun generateResponse(userQuery: String): String {
        // 1. 검색 (Retrieve)
        val relevantDocs = retrievalManager.retrieve(userQuery)

        // 2. 프롬프트 구성 (Augment)
        val prompt = buildPrompt(userQuery, relevantDocs)

        // 3. 답변 생성 (Generate)
        val rawResponse = inferenceModel.generateResponse(prompt) ?: "답변을 생성하지 못했습니다."

        // 4. 생성된 답변 필터링 적용
        return cleanRepetitiveText(rawResponse)
    }

    // 중복 구절을 잘라내는 헬퍼 함수
    private fun cleanRepetitiveText(text: String): String {
        // 1. 단어가 연속으로 3번 이상 반복되면 그 이후를 잘라버림 (예: "수건으로 수건으로 수건으로")
        val wordRepeatRegex = Regex("""(\b\S+\b)(?:\s+\1){2,}""")
        val match = wordRepeatRegex.find(text)

        if (match != null) {
            // 반복이 시작되기 전까지만 텍스트를 남기고 "..." 처리
            return text.substring(0, match.range.first).trim() + "..."
        }
        return text.trim()
    }

    private fun buildPrompt(query: String, docs: List<RagChunk>): String {
        val contextText = if (docs.isEmpty()) {
            "관련 정보를 찾을 수 없습니다."
        } else {
            docs.joinToString("\n\n") { doc ->
                """
                [문서 제목: ${doc.docTitle}]
                ${doc.content}
                """.trimIndent()
            }
        }

        return """
            당신은 재난 안전 및 응급 처치 전문가입니다.
            아래 제공된 [참고 자료]를 바탕으로 사용자의 [질문]에 대해 정확하고 이해하기 쉽게 답변하세요.

            [참고 자료]
            $contextText

            [질문]
            $query
            
            [답변 지침]
            1. [참고 자료]에 있는 내용으로만 짧고 명확하게 답변하세요.
            2. 답변이 끝나면 반드시 문장을 종료하고, 절대로 같은 단어나 문장을 반복해서 말하지 마세요.

            [답변]
        """.trimIndent()
    }
}