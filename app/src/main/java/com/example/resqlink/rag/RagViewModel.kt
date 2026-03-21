package com.example.resqlink.rag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resqlink.data.store.ManualInstallStore
import com.example.resqlink.data.store.SearchHistoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import kotlinx.coroutines.launch

sealed interface ManualState {
    data object NotInstalled : ManualState
    data class Installing(val progress: String = "") : ManualState
    data object Ready : ManualState
    data class Error(val message: String) : ManualState
}

class RagViewModel(
    private val ragPipeline: RagPipeline,
    private val searchHistoryStore: SearchHistoryStore,
    private val manualInstallStore: ManualInstallStore,
    private val initializer: suspend ((String) -> Unit) -> String?
) : ViewModel() {

    private val _messages = MutableStateFlow<List<GuideChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isModelReady = MutableStateFlow(false)
    val isModelReady = _isModelReady.asStateFlow()

    private val _manualState = MutableStateFlow<ManualState>(ManualState.NotInstalled)
    val manualState = _manualState.asStateFlow()

    private val _searchHistory = MutableStateFlow(searchHistoryStore.getHistory())
    val searchHistory = _searchHistory.asStateFlow()

    init {
        if (manualInstallStore.isInstalled()) {
            performInitialization()
        }
    }

    fun installManual() {
        val current = _manualState.value
        if (current != ManualState.NotInstalled && current !is ManualState.Error) return
        performInitialization()
    }

    private fun performInitialization() {
        _manualState.value = ManualState.Installing()
        viewModelScope.launch {
            val error = initializer { progress ->
                _manualState.value = ManualState.Installing(progress)
            }
            if (error == null) {
                manualInstallStore.markInstalled()
                _isModelReady.value = true
                _manualState.value = ManualState.Ready
            } else {
                _manualState.value = ManualState.Error(error)
            }
        }
    }

    fun ask(query: String) {
        if (query.isBlank()) return

        searchHistoryStore.addQuery(query)
        _searchHistory.value = searchHistoryStore.getHistory()

        val userMessage = GuideChatMessage(content = query, isUser = true)
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val ragResponse = ragPipeline.generateResponse(query)
                Log.d("RagViewModel", "rawText: ${ragResponse.rawText}")
                val parsed = parseGuideAnswer(ragResponse.rawText, ragResponse.sourceTitles)
                val aiMessage = GuideChatMessage(
                    content = ragResponse.rawText,
                    isUser = false,
                    structuredAnswer = parsed
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                val errorMessage = GuideChatMessage(
                    content = "오류가 발생했습니다: ${e.message}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun clearHistory() {
        searchHistoryStore.clearHistory()
        _searchHistory.value = emptyList()
    }
}
