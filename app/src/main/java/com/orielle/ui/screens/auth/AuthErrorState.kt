package com.orielle.ui.screens.auth

/**
 * A sealed class to represent specific error states for our auth text fields.
 * This is more powerful than a simple String because it lets us handle
 * different types of errors in different ways in the UI.
 */
sealed class AuthFieldError {
    /** The field has no error. */
    object None : AuthFieldError()

    /** The field has a simple text-based error message. */
    data class SimpleError(val message: String) : AuthFieldError()

    /** A special error for when the email is already in use, which will trigger a custom UI. */
    data class EmailAlreadyInUse(val message: String) : AuthFieldError()
}