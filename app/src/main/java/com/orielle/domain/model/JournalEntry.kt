package com.orielle.domain.model

import java.util.Date

/**
 * Represents the core Journal Entry model within the domain layer.
 * This is a clean, platform-agnostic representation of a single reflection.
 *
 * @param id A unique identifier for the journal entry.
 * @param userId The ID of the user who created this entry.
 * @param title Optional title for the entry.
 * @param content The main text content of the journal entry.
 * @param timestamp The date and time the entry was created.
 * @param location Optional location where the entry was written.
 * @param mood An optional mood or emotional tag associated with the entry.
 * @param tags List of tags associated with this entry.
 * @param photoUrl Optional photo attachment URL.
 * @param promptText The prompt that inspired this entry (if any).
 * @param entryType The type of journal entry (prompt, free_write, quick_entry).
 */
data class JournalEntry(
    val id: String = "",
    val userId: String = "",
    val title: String? = null,
    val content: String = "",
    val timestamp: Date = Date(),
    val location: String? = null,
    val mood: String? = null,
    val tags: List<String> = emptyList(),
    val photoUrl: String? = null,
    val promptText: String? = null,
    val entryType: JournalEntryType = JournalEntryType.FREE_WRITE
)

/**
 * Enum representing different types of journal entries.
 */
enum class JournalEntryType {
    PROMPT,        // Entry based on a daily prompt
    FREE_WRITE,    // Free-form writing
    QUICK_ENTRY    // Quick, short entry
}
