package com.orielle.domain.model

/**
 * A custom exception to indicate that a user is trying to sign up
 * with an email that already exists.
 */
class EmailAlreadyInUseException : Exception("An account with this email address already exists.")

/**
 * A custom exception for when the password is too weak.
 */
class WeakPasswordException : Exception("Please choose a stronger password (at least 6 characters).")