package com.example.resqlink.rag


import com.example.resqlink.rag.database.RagChunk

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
        return inferenceModel.generateResponse(prompt)
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
            당신은 재난 안전 및 응급 처치 전문가 봇입니다.
            아래 제공된 [참고 자료]만을 바탕으로 사용자의 [질문]에 대해 정확하고 이해하기 쉽게 답변하세요.
            [참고 자료]에 없는 내용은 지어내지 말고 "제공된 매뉴얼에 해당 내용이 없습니다"라고 말하세요.

            [참고 자료]
            $contextText

            [질문]
            $query

            [답변]
        """.trimIndent()
    }
}