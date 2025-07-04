package com.orielle.domain.model

import java.util.Date

/**
 * Represents the core Journal Entry model within the domain layer.
 * This is a clean, platform-agnostic representation of a single reflection.
 *
 * @param id A unique identifier for the journal entry.
 * @param userId The ID of the user who created this entry.
 * @param content The main text content of the journal entry.
 * @param timestamp The date and time the entry was created.
 * @param mood An optional mood or emotional tag associated with the entry.
 */
data class JournalEntry(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val mood: String? = null
)
