package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val displayName: String?,
    val hasAgreedToTerms: Boolean = false // New field for the local cache
)
