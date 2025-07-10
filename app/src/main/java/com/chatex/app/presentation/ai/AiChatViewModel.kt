package com.chatex.app.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.repository.GeminiRepository
import com.chatex.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<AiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)

data class AiMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Add user message
        val userMessage = AiMessage(
            text = message,
            isFromUser = true
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                isLoading = true,
                inputText = ""
            )
        }

        // Prepare conversation history
        val conversation = _uiState.value.messages.map { msg ->
            if (msg.isFromUser) "user" to msg.text else "model" to msg.text
        }.toMutableList()
        
        // Add current user message to conversation
        conversation.add("user" to message)

        viewModelScope.launch {
            geminiRepository.generateChatResponse(conversation).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        val aiMessage = AiMessage(
                            text = result.data,
                            isFromUser = false
                        )
                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages + aiMessage,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
