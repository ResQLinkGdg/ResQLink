package com.example.resqlink.rag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.rag.database.ManualSearchManager
import com.example.resqlink.rag.generation.GenAiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RagViewModel(
    private val searchManager: ManualSearchManager,
    private val genAiManager: GenAiManager
) : ViewModel() {

    fun askQuestion(userQuery: String) {
        // viewModelScope를 사용하여 코루틴을 시작해야 suspend 함수를 호출할 수 있습니다.
        viewModelScope.launch(Dispatchers.IO) {
            // 유사도 검색을 통해 관련 매뉴얼 추출
            val topKManuals = searchManager.searchTopK(userQuery, k = 2)

            if (topKManuals.isNotEmpty()) {
                // 프롬프트 구성
                val finalPrompt = "질문: $userQuery \n 정보: ${topKManuals[0].content}"
                // LLM 답변 생성
                val answer = genAiManager.generateResponse(finalPrompt)
                // UI 업데이트
                withContext(Dispatchers.Main) {
                }
            }
        }
    }
}