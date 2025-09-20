package com.orielle.ui.screens.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.repository.ChatRepository
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class AskUiState(
    val messages: List<com.orielle.domain.model.ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val showChoiceModal: Boolean = false,
    val showTaggingModal: Boolean = false,
    val currentTags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AskViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskUiState())
    val uiState: StateFlow<AskUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = null

    fun getCurrentConversationId(): String? = currentConversationId

    private val orielleResponses = listOf(
        "I hear you. Can you tell me more about what you're feeling right now?",
        "That sounds really meaningful. What made that moment stand out to you?",
        "I'm here with you. Take your time to share whatever feels right.",
        "It's okay to feel that way. Sometimes just acknowledging our feelings is the first step.",
        "Thank you for sharing that with me. What would help you feel supported right now?",
        "I can sense that this is important to you. What does this experience mean to you?",
        "That takes courage to share. How are you taking care of yourself through this?",
        "I'm listening. Sometimes talking through our thoughts can bring clarity.",
        "Your feelings are valid. What do you think would help you move forward?",
        "I appreciate you trusting me with this. What support do you need right now?"
    )

    init {
        // Add welcome message to UI only (not saved to database)
        addMessageToUI(
            ChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                conversationId = "", // Will be set when conversation is created
                content = "Hello! I'm here to listen and support you. What's on your mind today?",
                isFromUser = false,
                timestamp = Date(),
                messageType = "text"
            )
        )
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId == null) {
                    Timber.e("No user ID found for sending message")
                    return@launch
                }

                // Create conversation if it doesn't exist
                if (currentConversationId == null) {
                    val conversation = ChatConversation(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "Ask Orielle Conversation",
                        createdAt = Date(),
                        updatedAt = Date(),
                        tags = _uiState.value.currentTags,
                        messageCount = 0,
                        lastMessagePreview = text.take(50)
                    )

                    val saveResult = chatRepository.saveConversation(conversation)
                    when (saveResult) {
                        is com.orielle.domain.model.Response.Success -> {
                            currentConversationId = conversation.id
                            Timber.d("Created new conversation: ${conversation.id}")
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(saveResult.exception, "Failed to create conversation")
                            return@launch
                        }
                        else -> return@launch
                    }
                }

                // Create and save user message
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConversationId!!,
                    content = text,
                    isFromUser = true,
                    timestamp = Date(),
                    messageType = "text"
                )

                // Add to UI immediately
                addMessageToUI(userMessage)

                // Save to database
                val saveMessageResult = chatRepository.saveMessage(userMessage)
                when (saveMessageResult) {
                    is com.orielle.domain.model.Response.Success -> {
                        Timber.d("Saved user message: ${userMessage.id}")
                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        Timber.e(saveMessageResult.exception, "Failed to save user message")
                    }
                    else -> { }
                }

                // Show typing indicator and generate response
                showTypingIndicator()
                generateOrielleResponse()

            } catch (e: Exception) {
                Timber.e(e, "Error in sendMessage")
            }
        }
    }

    private fun addMessageToUI(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message
        )
    }

    private fun showTypingIndicator() {
        _uiState.value = _uiState.value.copy(isTyping = true)
    }

    private fun hideTypingIndicator() {
        _uiState.value = _uiState.value.copy(isTyping = false)
    }

    private fun generateOrielleResponse() {
        viewModelScope.launch {
            try {
                // Simulate thinking time
                delay(1500L + (0..1000).random().toLong())

                hideTypingIndicator()

                val response = orielleResponses.random()
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConversationId ?: "",
                    content = response,
                    isFromUser = false,
                    timestamp = Date(),
                    messageType = "text"
                )

                // Add to UI immediately
                addMessageToUI(aiMessage)

                // Save to database if conversation exists
                if (currentConversationId != null) {
                    val saveResult = chatRepository.saveMessage(aiMessage)
                    when (saveResult) {
                        is com.orielle.domain.model.Response.Success -> {
                            Timber.d("Saved AI message: ${aiMessage.id}")
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(saveResult.exception, "Failed to save AI message")
                        }
                        else -> { }
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Error in generateOrielleResponse")
            }
        }
    }

    fun showChoiceModal() {
        _uiState.value = _uiState.value.copy(showChoiceModal = true)
    }

    fun hideChoiceModal() {
        _uiState.value = _uiState.value.copy(showChoiceModal = false)
    }

    fun letItGo() {
        hideChoiceModal()
        // Reset conversation state
        currentConversationId = null

        // Clear conversation
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    id = "welcome_${System.currentTimeMillis()}",
                    conversationId = "",
                    content = "Hello! I'm here to listen and support you. What's on your mind today?",
                    isFromUser = false,
                    timestamp = Date(),
                    messageType = "text"
                )
            ),
            currentTags = emptyList()
        )
    }

    fun addTags(tags: List<String>) {
        _uiState.value = _uiState.value.copy(currentTags = tags)

        // Update conversation with tags if it exists
        currentConversationId?.let { conversationId ->
            viewModelScope.launch {
                try {
                    // TODO: Add updateConversationTags method to repository
                    Timber.d("Would update conversation $conversationId with tags: $tags")
                } catch (e: Exception) {
                    Timber.e(e, "Error updating conversation tags")
                }
            }
        }
    }

    fun showTaggingModal() {
        _uiState.value = _uiState.value.copy(showTaggingModal = true)
    }

    fun hideTaggingModal() {
        _uiState.value = _uiState.value.copy(showTaggingModal = false)
    }

    fun isFirstTimeUser(): Boolean {
        return !sessionManager.hasSeenPrivacyCoachMark()
    }

    fun markPrivacyCoachMarkSeen() {
        sessionManager.setPrivacyCoachMarkSeen()
    }

    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(0..999).random()}"
    }
}

// Extension functions for SessionManager
private fun SessionManager.hasSeenPrivacyCoachMark(): Boolean {
    // This would be implemented in your actual SessionManager
    // For now, return false to always show on first use
    return false
}

private fun SessionManager.setPrivacyCoachMarkSeen() {
    // This would be implemented in your actual SessionManager
    // Store a flag that privacy coach mark has been seen
}
