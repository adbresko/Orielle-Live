package com.orielle.ui.util

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object Navigate : UiEvent()
    object NavigateUp : UiEvent()
    // Add more events as needed
} 