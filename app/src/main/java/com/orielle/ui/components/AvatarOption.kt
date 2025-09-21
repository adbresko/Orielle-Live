package com.orielle.ui.components

// Avatar library data class
data class AvatarOption(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val emoji: String? = null,
    val isPremium: Boolean = false
)
