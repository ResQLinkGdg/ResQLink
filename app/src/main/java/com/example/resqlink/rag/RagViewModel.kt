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
        viewModelScope.launch {
            _isLoading.value = true
            _answer.value = "답변 생성 중..."

            val response = ragPipeline.generateResponse(query)

            _answer.value = response
            _isLoading.value = false
        }
    }
}