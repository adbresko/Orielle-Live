package com.orielle.ui.screens.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AskUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val showChoiceModal: Boolean = false,
    val currentTags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AskViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskUiState())
    val uiState: StateFlow<AskUiState> = _uiState.asStateFlow()

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
        // Add welcome message
        addMessage(
            ChatMessage(
                id = "welcome",
                text = "Hello! I'm here to listen and support you. What's on your mind today?",
                isFromUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        // Add user message
        addMessage(
            ChatMessage(
                id = generateMessageId(),
                text = text,
                isFromUser = true
            )
        )

        // Show typing indicator and generate response
        showTypingIndicator()
        generateOrielleResponse()
    }

    private fun addMessage(message: ChatMessage) {
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
            // Simulate thinking time
            delay(1500L + (0..1000).random().toLong())

            hideTypingIndicator()

            val response = orielleResponses.random()
            addMessage(
                ChatMessage(
                    id = generateMessageId(),
                    text = response,
                    isFromUser = false
                )
            )
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
        // Clear conversation
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    id = "welcome",
                    text = "Hello! I'm here to listen and support you. What's on your mind today?",
                    isFromUser = false
                )
            ),
            currentTags = emptyList()
        )
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
