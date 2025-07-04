package com.orielle.domain.model

/**
 * Represents the core User model within the domain layer.
 * This is a clean, platform-agnostic representation of a user.
 *
 * @param uid The unique identifier for the user, typically from Firebase Auth.
 * @param email The user's email address.
 * @param displayName A publicly visible name for the user.
 */
data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null
)
