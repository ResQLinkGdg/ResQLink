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
    val lines = text.lines()
        .map { it.trim().replace("：", ":") }  // 전각 콜론 → 반각 콜론
        .filter { it.isNotBlank() }

    var title = ""
    var summary = ""
    val keyActions = mutableListOf<String>()
    val detailSteps = mutableListOf<String>()
    var warning = ""

    var matched = false
    var lastMatchedField = ""  // 직전에 매칭된 필드 이름 추적
    val actionPrefixRegex = Regex("""^행동\s*\d*\s*[:]\s*""")
    val numberedListRegex = Regex("""^\d+[.)]\s+(.+)""")
    val bulletRegex = Regex("""^[-*•▶]\s+(.+)""")

    // 동의어 접두사 매핑
    val titlePrefixes = listOf("제목:", "주제:", "상황:")
    val summaryPrefixes = listOf("요약:", "설명:", "개요:")
    val actionPrefixes = listOf("행동:", "조치:", "대처:", "방법:", "대응:")
    val warningPrefixes = listOf("주의:", "주의사항:", "경고:", "유의:")

    for (line in lines) {
        when {
            titlePrefixes.any { line.startsWith(it) } -> {
                title = titlePrefixes.fold(line) { acc, prefix -> acc.removePrefix(prefix) }.trim()
                matched = true; lastMatchedField = "제목"
            }
            summaryPrefixes.any { line.startsWith(it) } -> {
                summary = summaryPrefixes.fold(line) { acc, prefix -> acc.removePrefix(prefix) }.trim()
                matched = true; lastMatchedField = "요약"
            }
            actionPrefixes.any { line.startsWith(it) } || actionPrefixRegex.containsMatchIn(line) -> {
                val content = actionPrefixes.fold(
                    line.replace(actionPrefixRegex, "")
                ) { acc, prefix -> acc.removePrefix(prefix) }.trim()
                if (content.isNotBlank()) keyActions.add(content)
                matched = true; lastMatchedField = "행동"
            }
            line.startsWith("단계:") -> {
                detailSteps.add(line.removePrefix("단계:").trim())
                matched = true; lastMatchedField = "단계"
            }
            warningPrefixes.any { line.startsWith(it) } -> {
                warning = warningPrefixes.fold(line) { acc, prefix -> acc.removePrefix(prefix) }.trim()
                matched = true; lastMatchedField = "주의"
            }
            lastMatchedField == "행동" && numberedListRegex.matches(line) -> {
                val content = numberedListRegex.find(line)?.groupValues?.get(1)?.trim()
                if (!content.isNullOrBlank()) keyActions.add(content)
            }
            lastMatchedField == "행동" && bulletRegex.matches(line) -> {
                val content = bulletRegex.find(line)?.groupValues?.get(1)?.trim()
                if (!content.isNullOrBlank()) keyActions.add(content)
            }
            else -> { /* lastMatchedField 유지 — 비매칭 줄이 있어도 컨텍스트 리셋하지 않음 */ }
        }
    }

    if (!matched) return null

    return StructuredGuideAnswer(
        title = if (isPlaceholder(title)) "" else title.ifBlank { "안내" },
        summary = if (isPlaceholder(summary)) "" else summary,
        key_actions = keyActions.filterNot { isPlaceholder(it) }.distinct().take(10),
        detail_steps = detailSteps.filterNot { isPlaceholder(it) }.distinct(),
        warning = if (isPlaceholder(warning)) "" else warning,
        source_titles = fallbackSources
    )
}

private fun trySalvagePlainText(
    text: String,
    fallbackSources: List<String>
): StructuredGuideAnswer {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val numberedRegex = Regex("""^\d+[.)]\s+(.+)""")
    val bulletRegex = Regex("""^[-*•▶]\s+(.+)""")

    val title = lines.firstOrNull() ?: "안내"
    val actions = mutableListOf<String>()
    val otherLines = mutableListOf<String>()

    for (line in lines.drop(1)) {
        val numberedMatch = numberedRegex.find(line)
        val bulletMatch = bulletRegex.find(line)
        when {
            numberedMatch != null -> actions.add(numberedMatch.groupValues[1].trim())
            bulletMatch != null -> actions.add(bulletMatch.groupValues[1].trim())
            else -> otherLines.add(line)
        }
    }

    // 최종 fallback: 번호/bullet 목록이 없으면 문장 분리로 key_actions 생성
    if (actions.isEmpty()) {
        val allText = otherLines.joinToString(" ")
        val sentences = allText.split(Regex("""[.。]\s*|\n"""))
            .map { it.trim() }
            .filter { it.length in 5..100 }
            .take(5)
        actions.addAll(sentences)
        otherLines.clear()
    }

    val summary = otherLines.joinToString(" ")

    return StructuredGuideAnswer(
        title = title,
        summary = summary,
        key_actions = actions.distinct().take(10),
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
