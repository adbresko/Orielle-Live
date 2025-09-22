package com.orielle.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Scalable schema manager that handles database changes automatically
 * No need to create new migration classes for each field change
 */
object SchemaManager {

    /**
     * Apply all schema changes in a single, idempotent migration
     * This method can be called multiple times safely
     */
    fun applySchemaChanges(database: SupportSQLiteDatabase) {
        try {
            // Mood Check-ins schema changes
            ensureMoodCheckInSchema(database)

            // Future schema changes can be added here
            // ensureJournalSchema(database)
            // ensureUserSchema(database)
            // etc.

            Timber.d("✅ Schema changes applied successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error applying schema changes")
            throw e
        }
    }

    /**
     * Ensure mood check-ins table has all required fields and constraints
     */
    private fun ensureMoodCheckInSchema(database: SupportSQLiteDatabase) {
        // Add dateKey column if it doesn't exist
        try {
            database.execSQL("ALTER TABLE mood_check_ins ADD COLUMN dateKey TEXT NOT NULL DEFAULT ''")
            Timber.d("Added dateKey column to mood_check_ins")
        } catch (e: Exception) {
            // Column already exists, that's fine
            Timber.d("dateKey column already exists in mood_check_ins")
        }

        // Populate dateKey for existing records
        database.execSQL("""
            UPDATE mood_check_ins 
            SET dateKey = strftime('%Y-%m-%d', timestamp/1000, 'unixepoch')
            WHERE dateKey = '' OR dateKey IS NULL
        """.trimIndent())

        // Create unique index for one check-in per day per user
        database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_mood_check_ins_user_date 
            ON mood_check_ins(userId, dateKey)
        """.trimIndent())

        Timber.d("Mood check-ins schema ensured")
    }

    // Future methods for other entities can be added here:
    // private fun ensureJournalSchema(database: SupportSQLiteDatabase) { ... }
    // private fun ensureUserSchema(database: SupportSQLiteDatabase) { ... }
    // etc.

    /**
     * Example: How to add a new field to any table in the future
     * Just add a new method here and call it from applySchemaChanges()
     *
     * private fun ensureNewFieldSchema(database: SupportSQLiteDatabase) {
     *     try {
     *         database.execSQL("ALTER TABLE table_name ADD COLUMN new_field TEXT DEFAULT ''")
     *     } catch (e: Exception) {
     *         // Field already exists, that's fine
     *     }
     * }
     */
}
