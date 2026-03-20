package com.example.resqlink.rag

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StructuredGuideAnswer(
    val title: String,
    val summary: String = "",
    val key_actions: List<String> = emptyList(),
    val detail_steps: List<String> = emptyList(),
    val warning: String = "",
    val source_titles: List<String> = emptyList()
)

private val lenientJson = Json { ignoreUnknownKeys = true }

fun parseGuideAnswer(
    rawText: String,
    fallbackSources: List<String>
): StructuredGuideAnswer? {
    var trimmed = rawText.trim()
    if (trimmed.isBlank()) return null

    // Preamble 제거: 모델이 질문을 되풀이하거나 서문을 다는 경우 "제목:" 시작점까지 스킵
    val titleIndex = trimmed.indexOf("제목:")
    if (titleIndex > 0) {
        trimmed = trimmed.substring(titleIndex)
    }

    // 1단계: JSON 파싱 (기존 호환성)
    tryParseJson(trimmed, fallbackSources)?.let { return it }

    // 2단계: 구분자 기반 파싱
    tryParseDelimited(trimmed, fallbackSources)?.let { return it }

    // 3단계: 일반 텍스트 salvage
    return trySalvagePlainText(trimmed, fallbackSources)
}

private fun tryParseJson(
    text: String,
    fallbackSources: List<String>
): StructuredGuideAnswer? {
    return try {
        val jsonStr = extractJsonBlock(text) ?: return null
        val parsed = lenientJson.decodeFromString<StructuredGuideAnswer>(jsonStr)
        if (fallbackSources.isNotEmpty()) {
            parsed.copy(source_titles = fallbackSources)
        } else {
            parsed
        }
    } catch (_: Exception) {
        null
    }
}

private fun tryParseDelimited(
    text: String,
    fallbackSources: List<String>
): StructuredGuideAnswer? {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

    var title = ""
    var summary = ""
    val keyActions = mutableListOf<String>()
    val detailSteps = mutableListOf<String>()
    var warning = ""

    var matched = false
    for (line in lines) {
        when {
            line.startsWith("제목:") -> { title = line.removePrefix("제목:").trim(); matched = true }
            line.startsWith("요약:") -> { summary = line.removePrefix("요약:").trim(); matched = true }
            line.startsWith("행동:") -> { keyActions.add(line.removePrefix("행동:").trim()); matched = true }
            line.startsWith("단계:") -> { detailSteps.add(line.removePrefix("단계:").trim()); matched = true }
            line.startsWith("주의:") -> { warning = line.removePrefix("주의:").trim(); matched = true }
        }
    }

    if (!matched) return null

    return StructuredGuideAnswer(
        title = if (isPlaceholder(title)) "" else title.ifBlank { "안내" },
        summary = if (isPlaceholder(summary)) "" else summary,
        key_actions = keyActions.filterNot { isPlaceholder(it) },
        detail_steps = detailSteps.filterNot { isPlaceholder(it) },
        warning = if (isPlaceholder(warning)) "" else warning,
        source_titles = fallbackSources
    )
}

private fun trySalvagePlainText(
    text: String,
    fallbackSources: List<String>
): StructuredGuideAnswer {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val title = lines.firstOrNull() ?: "안내"
    val summary = if (lines.size > 1) lines.drop(1).joinToString(" ") else ""

    return StructuredGuideAnswer(
        title = title,
        summary = summary,
        key_actions = emptyList(),
        detail_steps = emptyList(),
        warning = "",
        source_titles = fallbackSources
    )
}

private fun isPlaceholder(text: String): Boolean {
    val t = text.trim()
    if (t.isEmpty()) return false
    return t.matches(Regex("^\\(.*\\)$")) ||
           t == "..." ||
           t.matches(Regex("^\\.{2,}$")) ||
           t.matches(Regex("^·{2,}$")) ||
           t == "(제목)" || t == "(한 줄 요약)" || t == "(주의사항)" ||
           t == "(내용)" || t == "(없음)" || t == "(설명)" ||
           t == "....." || t == "___"
}

private fun extractJsonBlock(text: String): String? {
    val start = text.indexOf('{')
    if (start == -1) return null
    var depth = 0
    for (i in start until text.length) {
        when (text[i]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) return text.substring(start, i + 1)
            }
        }
    }
    return null
}
