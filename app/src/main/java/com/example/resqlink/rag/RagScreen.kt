package com.example.resqlink.rag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RagScreen(viewModel: RagViewModel) {
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 입력창의 텍스트 상태
    var queryText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
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