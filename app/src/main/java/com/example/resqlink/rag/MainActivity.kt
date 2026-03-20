package com.example.resqlink.rag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqlink.data.store.ManualInstallStore
import com.example.resqlink.data.store.SearchHistoryStore
import com.example.resqlink.rag.database.DataPackLoader
import com.example.resqlink.rag.generation.InferenceModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainAcivity : ComponentActivity() {

    private lateinit var viewModel: RagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. RAG 컴포넌트 초기화
        val dataPackLoader = DataPackLoader(this)
        val embeddingHelper = EmbeddingHelper(this)
        val inferenceModel = InferenceModel(this)

        // 파이프라인 조립
        val retrievalManager = RetrievalManager(dataPackLoader, embeddingHelper)
        val ragPipeline = RagPipeline(inferenceModel, retrievalManager)
        val manualInstallStore = ManualInstallStore(this)

        viewModel = RagViewModel(
            ragPipeline = ragPipeline,
            searchHistoryStore = SearchHistoryStore(this),
            manualInstallStore = manualInstallStore,
            initializer = { onProgress ->
                coroutineScope {
                    onProgress("데이터 로딩 중...")
                    launch { inferenceModel.initialize() }
                    launch { embeddingHelper.initialize() }
                    launch { dataPackLoader.loadDataPack() }
                }
                if (inferenceModel.isReady) null
                else inferenceModel.errorMessage ?: "알 수 없는 오류"
            }
        )

        // 화면 설정 (Compose)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RagScreen(viewModel)
                }
            }
        }
    }
}

// 3. UI 구성 요소
