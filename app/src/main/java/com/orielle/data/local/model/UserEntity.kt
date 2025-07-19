package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val displayName: String?,
    val firstName: String?, // Added for personalized greetings
    val lastName: String?, // Added for future use
    val hasAgreedToTerms: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false
)
