package com.orielle.domain.model

/**
 * Represents the core User model within the domain layer.
 * This is a clean, platform-agnostic representation of a user.
 */
data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val firstName: String? = null, // Added for personalized greetings
    val lastName: String? = null, // Added for future use
    val hasAgreedToTerms: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false
)
