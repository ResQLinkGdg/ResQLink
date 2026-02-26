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
import androidx.lifecycle.lifecycleScope
import com.example.resqlink.rag.database.DataPackLoader
import com.example.resqlink.rag.generation.InferenceModel
import kotlinx.coroutines.launch

class MainAcivity : ComponentActivity() {

    private lateinit var viewModel: RagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. RAG 컴포넌트 초기화
        val dataPackLoader = DataPackLoader(this)
        val embeddingHelper = EmbeddingHelper(this)

        // [변경] context(this)를 생성자에 전달합니다.
        val inferenceModel = InferenceModel(this)

        // 2. 비동기 초기화 (데이터팩 로드 + 모델 로드)
        lifecycleScope.launch {
            // 동시에 로드하여 속도 향상
            launch { dataPackLoader.loadDataPack() }
            launch { embeddingHelper.initialize() }
            launch { inferenceModel.initialize() } // [변경] LLM 모델 로드 호출
        }

        // 파이프라인 조립
        val retrievalManager = RetrievalManager(dataPackLoader, embeddingHelper)
        val ragPipeline = RagPipeline(inferenceModel, retrievalManager)
        viewModel = RagViewModel(ragPipeline)

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
