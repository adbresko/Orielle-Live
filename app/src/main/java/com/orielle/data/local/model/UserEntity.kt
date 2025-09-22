package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val displayName: String?,
    val hasAgreedToTerms: Boolean = false,
    val premium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Profile image fields for efficient caching
    val profileImageUrl: String? = null,
    val localImagePath: String? = null,
    val selectedAvatarId: String? = null,
    val notificationsEnabled: Boolean = true,
    val twoFactorEnabled: Boolean = false
)
