package com.example.resqlink.rag.database


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// chunks.jsonl의 각 줄에 해당하는 데이터 클래스
@Serializable
data class RagChunk(
    val docId: String,
    val docTitle: String,
    val content: String,
    val chunkId: String,
    // 필요에 따라 pageStart, sourceUrl 등 추가
)

// manifest.json의 메타데이터
@Serializable
data class EmbeddingsMeta(
    val dim: Int,
    val count: Int,
    val dtype: String
)