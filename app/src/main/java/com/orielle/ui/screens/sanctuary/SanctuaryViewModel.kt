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
import com.orielle.ui.util.UiEvent

@HiltViewModel
class SanctuaryViewModel @Inject constructor() : ViewModel() {

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
                // In a real implementation, this would call a Gemini API UseCase.
                // For now, we simulate a delay and provide a canned, empathetic response.
                delay(1500) // Simulate network latency for the AI response
                _aiResponse.value = "Thank you for sharing that space with me. It's a gift to witness your inner world."
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
