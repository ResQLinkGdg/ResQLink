package com.example.resqlink.rag


import com.example.resqlink.rag.database.RagChunk
import com.example.resqlink.rag.generation.InferenceModel

data class RagResponse(
    val rawText: String,
    val sourceTitles: List<String>
)

class RagPipeline(
    private val inferenceModel: InferenceModel,
    private val retrievalManager: RetrievalManager
) {
    suspend fun generateResponse(userQuery: String): RagResponse {
        // 1. 검색 (Retrieve)
        val relevantDocs = retrievalManager.retrieve(userQuery)

        // 2. 프롬프트 구성 (Augment)
        val prompt = buildPrompt(userQuery, relevantDocs)

        // 3. 답변 생성 (Generate)
        val rawText = inferenceModel.generateResponse(prompt) ?: "답변을 생성하지 못했습니다."

        val sourceTitles = relevantDocs.map { it.docTitle }.distinct()

        return RagResponse(rawText = rawText, sourceTitles = sourceTitles)
    }

    private fun buildPrompt(query: String, docs: List<RagChunk>): String {
        val maxChunkLength = 400
        val maxTotalContextChars = 1800

        val contextText = if (docs.isEmpty()) {
            "관련 정보 없음"
        } else {
            val sb = StringBuilder()
            var totalChars = 0
            for ((index, doc) in docs.withIndex()) {
                val cleaned = cleanChunkContent(doc.content, maxChunkLength)
                val label = doc.section ?: doc.docTitle
                val entry = "[자료${index + 1}: $label] $cleaned"
                if (totalChars + entry.length > maxTotalContextChars) break
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(entry)
                totalChars += entry.length
            }
            sb.toString().ifEmpty { "관련 정보 없음" }
        }

        return buildString {
            // 역할 + 근거 기반 지시
            append("당신은 응급상황 가이드입니다. ")
            append("아래 참고자료만 사용하여 답하세요. ")
            append("참고자료에 없는 내용은 답하지 마세요.\n\n")
            // 참고자료
            append(contextText)
            append("\n\n")
            // 원샷 예시 (1B 모델에 필수)
            append("예시:\n")
            append("제목: 화상 응급처치\n")
            append("요약: 화상 부위를 즉시 흐르는 찬물로 식힌다.\n")
            append("행동: 화상 부위를 흐르는 찬물에 10분 이상 식힌다\n")
            append("행동: 깨끗한 거즈로 덮는다\n")
            append("주의: 물집을 터뜨리지 않는다\n\n")
            // 질문
            append("질문: ")
            append(query)
            append("\n\n위 형식대로 답하세요:\n")
        }
    }

    private fun cleanChunkContent(raw: String, maxLength: Int): String {
        var cleaned = raw
            .replace(Regex("""\(cid:\d+\)"""), "")
            .replace(Regex("""\[page \d+\]"""), "")
            .replace(Regex("""\s{2,}"""), " ")
            .replace(Regex("""[·…]{3,}"""), " ")
            .trim()

        if (cleaned.length > maxLength) {
            val truncated = cleaned.take(maxLength)
            val lastBreak = maxOf(truncated.lastIndexOf('.'), truncated.lastIndexOf('\n'))
            cleaned = if (lastBreak > maxLength * 0.6) {
                truncated.substring(0, lastBreak + 1)
            } else truncated
        }
        return cleaned
    }
}
