package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a quote in the local Room database.
 * This class defines the schema for the 'quotes' table.
 *
 * @param id A unique identifier for the quote (from CSV quote_id).
 * @param quote The actual quote text.
 * @param source The source/author of the quote.
 * @param mood The mood category this quote belongs to.
 */
@Entity(
    tableName = "quotes",
    indices = [Index(value = ["mood"])]
)
data class QuoteEntity(
    @PrimaryKey
    val id: String,
    val quote: String,
    val source: String,
    val mood: String
)
