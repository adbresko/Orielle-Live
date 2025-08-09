package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orielle.domain.model.JournalEntryType
import java.util.Date

/**
 * Represents a single journal entry in the local Room database.
 * This class defines the schema for the 'journal_entries' table.
 *
 * @param id A unique identifier for the journal entry.
 * @param userId The ID of the user who created this entry.
 * @param title Optional title for the entry.
 * @param content The main text content of the journal entry.
 * @param timestamp The date and time the entry was created.
 * @param location Optional location where the entry was written.
 * @param mood An optional mood or emotional tag associated with the entry.
 * @param tags JSON string representation of tags list.
 * @param photoUrl Optional photo attachment URL.
 * @param promptText The prompt that inspired this entry (if any).
 * @param entryType The type of journal entry (stored as string).
 */
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String?,
    val content: String,
    val timestamp: Date,
    val location: String?,
    val mood: String?,
    val tags: String, // JSON string representation of List<String>
    val photoUrl: String?,
    val promptText: String?,
    val entryType: String // Stored as string, converted to/from JournalEntryType
)
