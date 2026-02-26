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
            val chunkList = mutableListOf<RagChunk>()
            context.assets.open("chunks.jsonl").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).forEachLine { line ->
                    if (line.isNotBlank()) {
                        try {
                            val chunk = Json { ignoreUnknownKeys = true }.decodeFromString<RagChunk>(line)
                            chunkList.add(chunk)
                        } catch (e: Exception) {
                            Log.w(TAG, "Chunk parsing error: ${e.message}")
                        }
                    }
                }
            }
            chunks = chunkList
            Log.d(TAG, "청크 로드 완료: ${chunks.size}개")

            // 3. 임베딩 벡터 로드 (embeddings.f16.bin)
            // f16(2byte) * dim * count 크기
            val expectedSize = 2 * dim * count
            val byteArray = ByteArray(expectedSize)
            context.assets.open("embeddings.f16.bin").use { it.read(byteArray) }

            val buffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
            val loadedEmbeddings = Array(count) { FloatArray(dim) }

            for (i in 0 until count) {
                for (j in 0 until dim) {
                    // Float16 to Float32 변환 (Android API 26+ Half, 혹은 수동 변환)
                    // 여기서는 간단히 Short로 읽어서 변환한다고 가정하거나,
                    // API 레벨에 따라 android.util.Half.toFloat(short) 사용
                    val halfFloat = buffer.short
                    loadedEmbeddings[i][j] = android.util.Half.toFloat(halfFloat)
                }
            }
            embeddings = loadedEmbeddings
            Log.d(TAG, "임베딩 로드 완료. 차원: $dim")

            isLoaded = true
        } catch (e: Exception) {
            Log.e(TAG, "데이터팩 로딩 실패", e)
        }
    }

    companion object {
        private const val TAG = "DataPackLoader"
    }
}