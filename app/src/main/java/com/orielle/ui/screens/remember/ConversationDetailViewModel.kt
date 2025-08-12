package com.orielle.ui.screens.remember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.model.Response
import com.orielle.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load conversation details
                val conversationResponse = chatRepository.getConversationById(conversationId)
                when (conversationResponse) {
                    is Response.Success -> {
                        val conversation = conversationResponse.data
                        _uiState.value = _uiState.value.copy(
                            conversation = conversation
                        )

                        // Load messages for this conversation
                        conversation?.let { conv ->
                            chatRepository.getMessagesForConversation(conv.id)
                                .collect { messagesResponse ->
                                    when (messagesResponse) {
                                        is Response.Success -> {
                                            _uiState.value = _uiState.value.copy(
                                                messages = messagesResponse.data.sortedBy { it.timestamp }
                                            )
                                        }
                                        is Response.Failure -> {
                                            // Handle error
                                        }
                                        is Response.Loading -> {
                                            // Handle loading
                                        }
                                    }
                                }
                        }
                    }
                    is Response.Failure -> {
                        // Handle error
                    }
                    is Response.Loading -> {
                        // Handle loading
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

data class ConversationDetailUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)
