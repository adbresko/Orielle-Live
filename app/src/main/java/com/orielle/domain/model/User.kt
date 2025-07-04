package com.orielle.domain.model

/**
 * Represents the core User model within the domain layer.
 * This is a clean, platform-agnostic representation of a user.
 */
data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val hasAgreedToTerms: Boolean = false // New field to track TOC agreement
)
