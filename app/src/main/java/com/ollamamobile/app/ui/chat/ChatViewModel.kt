package com.ollamamobile.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ollamamobile.app.data.ChatMessage
import com.ollamamobile.app.data.ChatMessagePayload
import com.ollamamobile.app.data.OllamaModel
import com.ollamamobile.app.data.OllamaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val models: List<OllamaModel> = emptyList(),
    val selectedModel: String? = null,
    val loadingModels: Boolean = false,
    val modelsError: String? = null,
    val connectionError: String? = null,
    val isStreaming: Boolean = false,
    val baseUrl: String = OllamaRepository.DEFAULT_BASE_URL
)

class ChatViewModel(private val repository: OllamaRepository) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState(baseUrl = repository.getBaseUrl()))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null

    init {
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            _state.update { it.copy(loadingModels = true, modelsError = null) }
            repository.listModels()
                .onSuccess { models ->
                    _state.update {
                        it.copy(
                            models = models,
                            loadingModels = false,
                            modelsError = null,
                            selectedModel = it.selectedModel ?: models.firstOrNull()?.name
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loadingModels = false,
                            modelsError = e.message ?: "Failed to load models",
                            connectionError = "Is Ollama running? (e.g. in Termux: ollama serve)"
                        )
                    }
                }
        }
    }

    fun setSelectedModel(model: String?) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun sendMessage(text: String) {
        val content = text.trim()
        if (content.isEmpty()) return
        val model = _state.value.selectedModel ?: return
        streamJob?.cancel()
        val userMessage = ChatMessage(role = "user", content = content)
        _state.update {
            it.copy(
                messages = it.messages + userMessage + ChatMessage(role = "assistant", content = "", isStreaming = true),
                isStreaming = true,
                connectionError = null
            )
        }
        val fullHistory = _state.value.messages.dropLast(1) + userMessage
        val payload = fullHistory.map { ChatMessagePayload(it.role, it.content) }

        streamJob = viewModelScope.launch {
            repository.chatStream(model, payload)
                .catch { e ->
                    _state.update {
                        it.copy(
                            isStreaming = false,
                            messages = it.messages.dropLast(1) + (it.messages.lastOrNull()?.copy(isStreaming = false, content = it.messages.lastOrNull()?.content ?: "") ?: ChatMessage("assistant", "")),
                            connectionError = e.message ?: "Request failed"
                        )
                    }
                }
                .collect { result ->
                    result.onSuccess { delta ->
                        _state.update { s ->
                            val current = s.messages.lastOrNull()
                            val newContent = if (current?.role == "assistant" && current.isStreaming) {
                                current.content + delta
                            } else delta
                            val updatedAssistant = ChatMessage("assistant", newContent, isStreaming = true)
                            val list = s.messages.dropLast(1) + updatedAssistant
                            s.copy(messages = list)
                        }
                    }
                    result.onFailure { e ->
                        _state.update {
                            it.copy(
                                isStreaming = false,
                                connectionError = e.message ?: "Stream error"
                            )
                        }
                    }
                }
            _state.update {
                it.copy(
                    isStreaming = false,
                    messages = it.messages.map { msg ->
                        if (msg.role == "assistant" && msg.isStreaming) msg.copy(isStreaming = false)
                        else msg
                    }
                )
            }
        }
    }

    fun clearConnectionError() {
        _state.update { it.copy(connectionError = null, modelsError = null) }
    }

    fun newChat() {
        streamJob?.cancel()
        _state.update {
            it.copy(
                messages = emptyList(),
                connectionError = null,
                isStreaming = false
            )
        }
    }

    fun setBaseUrl(url: String) {
        repository.setBaseUrl(url)
        _state.update { it.copy(baseUrl = repository.getBaseUrl()) }
        loadModels()
    }
}

class ChatViewModelFactory(private val repository: OllamaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == ChatViewModel::class.java) return ChatViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
