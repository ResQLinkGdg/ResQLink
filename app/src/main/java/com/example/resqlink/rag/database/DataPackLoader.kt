package com.example.resqlink.rag.database

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DataPackLoader(private val context: Context) {

    // 메모리에 로드된 데이터
    var chunks: List<RagChunk> = emptyList()
        private set
    var embeddings: Array<FloatArray>? = null
        private set
    var isLoaded = false

    @SuppressLint("HalfFloat")
    suspend fun loadDataPack() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        try {
            Log.d(TAG, "데이터팩 로딩 시작...")

            // 1. 메타데이터 로드 (차원 수 확인용)
            val metaJson = context.assets.open("embeddings_meta.json").bufferedReader().use { it.readText() }
            val meta = Json { ignoreUnknownKeys = true }.decodeFromString<EmbeddingsMeta>(metaJson)
            val dim = meta.dim
            val count = meta.count

            // 2. 텍스트 청크 로드 (chunks.jsonl)
            val allChunks = mutableListOf<RagChunk>()
            context.assets.open("chunks.jsonl").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).forEachLine { line ->
                    if (line.isNotBlank()) {
                        try {
                            val chunk = Json { ignoreUnknownKeys = true }.decodeFromString<RagChunk>(line)
                            allChunks.add(chunk)
                        } catch (e: Exception) {
                            Log.w(TAG, "Chunk parsing error: ${e.message}")
                        }
                    }
                }
            }
            Log.d(TAG, "청크 파싱 완료: ${allChunks.size}개")

            // 3. 임베딩 벡터 로드 (embeddings.f16.bin)
            // f16(2byte) * dim * count 크기
            val expectedSize = 2 * dim * count
            val byteArray = ByteArray(expectedSize)
            context.assets.open("embeddings.f16.bin").use { it.read(byteArray) }

            val buffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
            val allEmbeddings = Array(count) { FloatArray(dim) }

            for (i in 0 until count) {
                for (j in 0 until dim) {
                    val halfFloat = buffer.short
                    allEmbeddings[i][j] = android.util.Half.toFloat(halfFloat)
                }
            }
            Log.d(TAG, "임베딩 로드 완료. 차원: $dim")

            // 4. OCR 깨진 청크 필터링: (cid:숫자) 패턴이 내용의 50% 이상인 청크 제거
            val cidPattern = Regex("""\(cid:\d+\)""")
            val validIndices = allChunks.indices.filter { i ->
                val content = allChunks[i].content
                val cidMatches = cidPattern.findAll(content).sumOf { it.value.length }
                cidMatches < content.length * 0.5
            }
            chunks = validIndices.map { allChunks[it] }
            embeddings = validIndices.map { allEmbeddings[it] }.toTypedArray()
            val filtered = allChunks.size - chunks.size
            Log.d(TAG, "청크 필터링 완료: ${chunks.size}개 유효 ($filtered 개 OCR 깨짐 제거)")

            isLoaded = true
        } catch (e: Exception) {
            Log.e(TAG, "데이터팩 로딩 실패", e)
        }
    }

    companion object {
        private const val TAG = "DataPackLoader"
    }
}