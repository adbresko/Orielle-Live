package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.data.local.model.UserEntity
import com.orielle.data.local.model.ChatConversationEntity
import com.orielle.data.local.model.ChatMessageEntity
import com.orielle.data.local.model.TagEntity
import com.orielle.data.local.model.ConversationTagCrossRef

@Database(
    entities = [
        UserEntity::class,
        JournalEntryEntity::class,
        MoodCheckInEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class,
        TagEntity::class,
        ConversationTagCrossRef::class
    ],
    version = 6, // Increment for enhanced journal entries
    exportSchema = false
)
@TypeConverters(Converters::class) // Add this to handle the Date type
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun moodCheckInDao(): MoodCheckInDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun tagDao(): TagDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add new columns to users table
                database.execSQL("ALTER TABLE users ADD COLUMN firstName TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN lastName TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN premium INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("ALTER TABLE users ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create chat_conversations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_conversations (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        title TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        tags TEXT NOT NULL DEFAULT '[]',
                        messageCount INTEGER NOT NULL DEFAULT 0,
                        isSaved INTEGER NOT NULL DEFAULT 0,
                        lastMessagePreview TEXT
                    )
                """.trimIndent())

                // Create chat_messages table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id TEXT NOT NULL PRIMARY KEY,
                        conversationId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        isFromUser INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        messageType TEXT NOT NULL DEFAULT 'text',
                        metadata TEXT,
                        FOREIGN KEY(conversationId) REFERENCES chat_conversations(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create tags table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        usageCount INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        isUserCreated INTEGER NOT NULL DEFAULT 1,
                        color TEXT,
                        description TEXT
                    )
                """.trimIndent())

                // Create conversation_tag_cross_ref table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS conversation_tag_cross_ref (
                        conversationId TEXT NOT NULL,
                        tagId TEXT NOT NULL,
                        PRIMARY KEY(conversationId, tagId),
                        FOREIGN KEY(conversationId) REFERENCES chat_conversations(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_conversationId ON chat_messages(conversationId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_timestamp ON chat_messages(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_tag_cross_ref_conversationId ON conversation_tag_cross_ref(conversationId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_tag_cross_ref_tagId ON conversation_tag_cross_ref(tagId)")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add new columns to journal_entries table
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN title TEXT")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN location TEXT")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN photoUrl TEXT")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN promptText TEXT")
                database.execSQL("ALTER TABLE journal_entries ADD COLUMN entryType TEXT NOT NULL DEFAULT 'FREE_WRITE'")

                // Create indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_timestamp ON journal_entries(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_entryType ON journal_entries(entryType)")
            }
        }
    }
}
