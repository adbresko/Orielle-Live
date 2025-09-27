package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_prompts")
data class JournalPromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moodCategory: String,
    val promptText: String
)
