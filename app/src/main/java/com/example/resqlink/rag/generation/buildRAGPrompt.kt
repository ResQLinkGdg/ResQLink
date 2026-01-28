package com.example.resqlink.rag.generation

import com.example.resqlink.rag.database.Manual

fun buildRAGPrompt(query: String, retrievedManuals: List<Manual>): String {
    // 2단계에서 찾은 매뉴얼들의 본문을 하나로 합칩니다.
    val contextText = retrievedManuals.joinToString("\n\n") {
        "제목: ${it.title}\n내용: ${it.content}"
    }

    return """
        당신은 재난상황에 처한 사람을 돕는 전문 상담원입니다.
        아래 [참고 정보]를 바탕으로 사용자의 [질문]에 친절하게 답변하세요.
        정보가 [참고 정보]에 없다면 아는 척하지 말고 "정보를 찾을 수 없습니다"라고 답하세요.

        [참고 정보]
        $contextText

        [질문]
        $query

        답변:
    """.trimIndent()
}