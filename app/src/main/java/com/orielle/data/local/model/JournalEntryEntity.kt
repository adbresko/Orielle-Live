package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a single journal entry in the local Room database.
 * This class defines the schema for the 'journal_entries' table.
 *
 * @param id A unique identifier for the journal entry.
 * @param userId The ID of the user who created this entry.
 * @param content The main text content of the journal entry.
 * @param timestamp The date and time the entry was created.
 * @param mood An optional mood or emotional tag associated with the entry.
 */
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val content: String,
    val timestamp: Date,
    val mood: String?
)
