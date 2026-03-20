package com.example.resqlink.rag

import java.util.UUID

data class GuideChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val structuredAnswer: StructuredGuideAnswer? = null
)
