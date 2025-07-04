package com.orielle.data.mapper

import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.domain.model.JournalEntry

/**
 * Converts a database entity (JournalEntryEntity) into a domain model (JournalEntry).
 */
fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = this.id,
        userId = this.userId,
        content = this.content,
        timestamp = this.timestamp,
        mood = this.mood
    )
}

/**
 * Converts a domain model (JournalEntry) into a database entity (JournalEntryEntity).
 */
fun JournalEntry.toEntity(): JournalEntryEntity {
    return JournalEntryEntity(
        id = this.id,
        userId = this.userId,
        content = this.content,
        timestamp = this.timestamp,
        mood = this.mood
    )
}
