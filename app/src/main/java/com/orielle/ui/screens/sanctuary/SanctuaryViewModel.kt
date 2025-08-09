package com.orielle.ui.screens.sanctuary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.orielle.domain.model.AppError
import timber.log.Timber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import com.orielle.ui.util.UiEvent
import com.orielle.domain.repository.ChatRepository
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage
import java.util.Date
import java.util.UUID

@HiltViewModel
class SanctuaryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val prompts = listOf(
        "What energy are you bringing into this moment?",
        "Take a breath. What does your heart wish to say?",
        "Let's create a quiet space. What's one word that describes your inner weather right now?"
    )

    // --- UI State ---
    private val _prompt = MutableStateFlow(prompts.random())
    val prompt = _prompt.asStateFlow()

    private val _userReflection = MutableStateFlow("")
    val userReflection = _userReflection.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse = _aiResponse.asStateFlow()

    private val _showCta = MutableStateFlow(false)
    val showCta = _showCta.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentConversationId: String? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unhandled coroutine exception in SanctuaryViewModel")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Unknown.toUserMessage()))
        }
    }

    // --- Event Handlers ---
    fun onReflectionChange(newText: String) {
        _userReflection.value = newText
    }

    fun submitReflection() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId == null) {
                    _eventFlow.emit(UiEvent.ShowSnackbar("User not authenticated"))
                    return@launch
                }

                val userMessage = _userReflection.value.trim()
                if (userMessage.isBlank()) {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Please enter a reflection"))
                    return@launch
                }

                // Create a conversation if this is the first message
                if (currentConversationId == null) {
                    val conversation = ChatConversation(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "Sanctuary Reflection",
                        createdAt = Date(),
                        updatedAt = Date(),
                        tags = listOf("sanctuary"),
                        messageCount = 0,
                        lastMessagePreview = userMessage.take(50)
                    )

                    val saveConversationResult = chatRepository.saveConversation(conversation)
                    when (saveConversationResult) {
                        is com.orielle.domain.model.Response.Success -> {
                            currentConversationId = conversation.id
                            Timber.d("Created new conversation: ${conversation.id}")
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(saveConversationResult.exception, "Failed to create conversation")
                            _eventFlow.emit(UiEvent.ShowSnackbar("Failed to save conversation"))
                            return@launch
                        }
                        else -> return@launch
                    }
                }

                // Save user message
                val userChatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConversationId!!,
                    content = userMessage,
                    isFromUser = true,
                    timestamp = Date(),
                    messageType = "text"
                )

                val saveUserMessageResult = chatRepository.saveMessage(userChatMessage)
                when (saveUserMessageResult) {
                    is com.orielle.domain.model.Response.Success -> {
                        Timber.d("Saved user message: ${userChatMessage.id}")
                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        Timber.e(saveUserMessageResult.exception, "Failed to save user message")
                        _eventFlow.emit(UiEvent.ShowSnackbar("Failed to save message"))
                        return@launch
                    }
                    else -> return@launch
                }

                // Simulate AI response with delay
                delay(1500) // Simulate network latency for the AI response
                val aiResponseText = "Thank you for sharing that space with me. It's a gift to witness your inner world."
                _aiResponse.value = aiResponseText

                // Save AI response message
                val aiChatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConversationId!!,
                    content = aiResponseText,
                    isFromUser = false,
                    timestamp = Date(),
                    messageType = "text"
                )

                val saveAiMessageResult = chatRepository.saveMessage(aiChatMessage)
                when (saveAiMessageResult) {
                    is com.orielle.domain.model.Response.Success -> {
                        Timber.d("Saved AI message: ${aiChatMessage.id}")
                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        Timber.e(saveAiMessageResult.exception, "Failed to save AI message")
                        // Don't return here, still show the UI response
                    }
                    else -> { /* Continue */ }
                }

                delay(1000) // A brief pause before showing the call to action
                _showCta.value = true

            } catch (e: Exception) {
                Timber.e(e, "Error in submitReflection")
                _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Unknown.toUserMessage()))
            }
        }
    }
}

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "No internet connection."
    AppError.Auth -> "Authentication failed."
    AppError.Database -> "A database error occurred."
    AppError.NotFound -> "Requested resource not found."
    AppError.Permission -> "You do not have permission to perform this action."
    is AppError.Custom -> this.message
    AppError.Unknown -> "An unknown error occurred."
}
