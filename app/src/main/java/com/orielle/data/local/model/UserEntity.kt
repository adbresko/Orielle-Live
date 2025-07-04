// path: app/src/main/java/com/orielle/data/local/model/UserEntity.kt
package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)