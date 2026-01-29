package com.example.resqlink.rag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RagViewModel(
    private val ragPipeline: RagPipeline
) : ViewModel() {

    private val _answer = MutableStateFlow("")
    val answer = _answer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun ask(query: String) {
        if (query.isBlank()) return // 빈 검색 방지

        viewModelScope.launch {
            _isLoading.value = true
            _answer.value = "" // 이전 답변 초기화

            try {
                val response = ragPipeline.generateResponse(query)
                _answer.value = response
            } catch (e: Exception) {
                _answer.value = "오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 팝업 닫을 때 상태 초기화용
    fun clearAnswer() {
        _answer.value = ""
        _isLoading.value = false
    }
}


//package com.example.resqlink.rag
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class RagViewModel(
//    private val ragPipeline: RagPipeline
//) : ViewModel() {
//
//    private val _answer = MutableStateFlow("")
//    val answer = _answer.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading = _isLoading.asStateFlow()
//
//    fun ask(query: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _answer.value = "답변 생성 중..."
//
//            val response = ragPipeline.generateResponse(query)
//
//            _answer.value = response
//            _isLoading.value = false
//        }
//    }
//}
