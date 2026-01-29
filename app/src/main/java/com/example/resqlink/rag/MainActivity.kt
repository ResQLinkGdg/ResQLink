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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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
@Composable
fun RagScreen(viewModel: RagViewModel) {
    // ViewModel의 상태를 관찰 (값이 바뀌면 화면이 자동 갱신됨)
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 입력창의 텍스트 상태
    var queryText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // 스크롤 가능하게 설정
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "재난 대응 매뉴얼 봇",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 질문 입력창
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            label = { Text("질문을 입력하세요") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 전송 버튼
        Button(
            onClick = {
                if (queryText.isNotBlank()) {
                    viewModel.ask(queryText)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // 로딩 중엔 버튼 비활성화
        ) {
            Text(if (isLoading) "답변 생성 중..." else "질문하기")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 로딩 인디케이터
        if (isLoading) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 답변 출력 영역
        if (answer.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "답변:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}