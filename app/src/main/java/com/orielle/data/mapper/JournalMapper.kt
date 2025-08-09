package com.orielle.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.JournalEntryType

/**
 * Converts a database entity (JournalEntryEntity) into a domain model (JournalEntry).
 */
fun JournalEntryEntity.toDomain(): JournalEntry {
    val gson = Gson()
    val tagListType = object : TypeToken<List<String>>() {}.type
    val tagList: List<String> = try {
        gson.fromJson(this.tags, tagListType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    return JournalEntry(
        id = this.id,
        userId = this.userId,
        title = this.title,
        content = this.content,
        timestamp = this.timestamp,
        location = this.location,
        mood = this.mood,
        tags = tagList,
        photoUrl = this.photoUrl,
        promptText = this.promptText,
        entryType = try {
            JournalEntryType.valueOf(this.entryType)
        } catch (e: Exception) {
            JournalEntryType.FREE_WRITE
        }
    )
}

/**
 * Converts a domain model (JournalEntry) into a database entity (JournalEntryEntity).
 */
fun JournalEntry.toEntity(): JournalEntryEntity {
    val gson = Gson()
    return JournalEntryEntity(
        id = this.id,
        userId = this.userId,
        title = this.title,
        content = this.content,
        timestamp = this.timestamp,
        location = this.location,
        mood = this.mood,
        tags = gson.toJson(this.tags),
        photoUrl = this.photoUrl,
        promptText = this.promptText,
        entryType = this.entryType.name
    )
}
