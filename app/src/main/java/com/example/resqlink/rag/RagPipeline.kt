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

        // 3. 답변 생성 (Generate) — prefill로 모델이 "제목:" 뒤부터 이어서 생성
        val generatedText = inferenceModel.generateResponse(prompt, prefill = "제목:") ?: "답변을 생성하지 못했습니다."
        val rawText = if (generatedText.trimStart().startsWith("제목:")) generatedText else "제목:$generatedText"

        val sourceTitles = relevantDocs.map { it.docTitle }.distinct()

        return RagResponse(rawText = rawText, sourceTitles = sourceTitles)
    }

    private fun buildPrompt(query: String, docs: List<RagChunk>): String {
        val maxChunkLength = 600
        val maxTotalContextChars = 3000

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
            append("참고자료에 없는 내용은 답하지 마세요. ")
            append("행동요령을 빠짐없이 모두 나열하세요.\n\n")
            // 참고자료
            append(contextText)
            append("\n\n")
            // 원샷 예시 (1B 모델에 필수 — 행동 3개로 축소하여 토큰 절약)
            append("예시:\n")
            append("제목: 심폐소생술(CPR)\n")
            append("요약: 의식과 호흡이 없는 환자에게 즉시 심폐소생술을 시행한다.\n")
            append("행동: 환자의 반응을 확인하고 119에 신고한다\n")
            append("행동: 가슴 중앙을 깍지 낀 손으로 30회 압박한다\n")
            append("행동: 30회 압박과 2회 호흡을 구급대 도착까지 반복한다\n")
            append("주의: 가슴 압박 깊이는 약 5cm, 속도는 분당 100~120회를 유지한다\n\n")
            // 질문
            append("질문: ")
            append(query)
            append("\n\n위 형식(제목/요약/행동/주의)대로 답하세요.")
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
