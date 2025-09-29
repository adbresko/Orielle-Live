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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            Timber.d("🔄 ConversationDetailViewModel: Loading conversation with ID: $conversationId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load conversation details
                Timber.d("🔄 ConversationDetailViewModel: Fetching conversation details...")
                val conversationResponse = chatRepository.getConversationById(conversationId)

                when (conversationResponse) {
                    is Response.Success -> {
                        val conversation = conversationResponse.data
                        Timber.d("✅ ConversationDetailViewModel: Conversation loaded successfully: ${conversation?.title}")
                        Timber.d("✅ ConversationDetailViewModel: Conversation tags: ${conversation?.tags}, isEmpty: ${conversation?.tags?.isEmpty()}")
                        _uiState.value = _uiState.value.copy(
                            conversation = conversation,
                            isLoading = false
                        )

                        // Load messages for this conversation
                        conversation?.let { conv ->
                            Timber.d("🔄 ConversationDetailViewModel: Loading messages for conversation: ${conv.id}")
                            chatRepository.getMessagesForConversation(conv.id)
                                .collect { messagesResponse ->
                                    when (messagesResponse) {
                                        is Response.Success -> {
                                            Timber.d("✅ ConversationDetailViewModel: Messages loaded successfully: ${messagesResponse.data.size} messages")
                                            _uiState.value = _uiState.value.copy(
                                                messages = messagesResponse.data.sortedBy { it.timestamp },
                                                isLoading = false
                                            )
                                        }
                                        is Response.Failure -> {
                                            Timber.e("❌ ConversationDetailViewModel: Failed to load messages: ${messagesResponse.exception?.message}")
                                            _uiState.value = _uiState.value.copy(
                                                error = "Failed to load messages: ${messagesResponse.exception?.message}",
                                                isLoading = false
                                            )
                                        }
                                        is Response.Loading -> {
                                            Timber.d("🔄 ConversationDetailViewModel: Loading messages...")
                                            // Keep loading state
                                        }
                                    }
                                }
                        } ?: run {
                            Timber.w("⚠️ ConversationDetailViewModel: Conversation is null, cannot load messages")
                            _uiState.value = _uiState.value.copy(
                                error = "Conversation not found",
                                isLoading = false
                            )
                        }
                    }
                    is Response.Failure -> {
                        Timber.e("❌ ConversationDetailViewModel: Failed to load conversation: ${conversationResponse.exception?.message}")
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load conversation: ${conversationResponse.exception?.message}",
                            isLoading = false
                        )
                    }
                    is Response.Loading -> {
                        Timber.d("🔄 ConversationDetailViewModel: Loading conversation...")
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ ConversationDetailViewModel: Exception while loading conversation")
                _uiState.value = _uiState.value.copy(
                    error = "Error loading conversation: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}

data class ConversationDetailUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
