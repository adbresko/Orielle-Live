package com.orielle.util

/**
 * A sealed class representing one-time events that the UI should handle.
 * Using a sealed class ensures we have a well-defined and restricted set of possible events.
 */
sealed class UiEvent {
    /**
     * Represents a navigation event.
     * @param route The destination route for the navigation.
     */
    data class Navigate(val route: String) : UiEvent()

    /**
     * Represents a command to show a snackbar message.
     * @param message The text to be displayed in the snackbar.
     */
    data class ShowSnackbar(val message: String) : UiEvent()

    /**
     * Represents a command to navigate back in the navigation stack.
     */
    object NavigateUp : UiEvent()
}