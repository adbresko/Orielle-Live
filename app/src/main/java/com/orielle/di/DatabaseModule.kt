package com.orielle.di

import android.content.Context
import androidx.room.Room
import com.orielle.data.local.OrielleDatabase
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.dao.MemoryEntryDao
import com.orielle.data.local.dao.QuoteDao
import com.orielle.data.local.dao.JournalPromptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideOrielleDatabase(
        @ApplicationContext context: Context
    ): OrielleDatabase {
        return Room.databaseBuilder(
            context,
            OrielleDatabase::class.java,
            "orielle_database_v2" // New database name to force fresh start
        )
            .fallbackToDestructiveMigration() // Recreate database from scratch - safest approach
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: OrielleDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Provides a singleton instance of the JournalDao.
     * @param database The OrielleDatabase instance provided by Hilt.
     * @return An instance of JournalDao.
     */
    @Provides
    @Singleton
    fun provideJournalDao(database: OrielleDatabase): JournalDao {
        return database.journalDao()
    }

    @Provides
    @Singleton
    fun provideMoodCheckInDao(db: OrielleDatabase): MoodCheckInDao = db.moodCheckInDao()

    @Provides
    @Singleton
    fun provideChatConversationDao(database: OrielleDatabase): ChatConversationDao {
        return database.chatConversationDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: OrielleDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: OrielleDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideMemoryEntryDao(database: OrielleDatabase): MemoryEntryDao {
        return database.memoryEntryDao()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: OrielleDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    @Singleton
    fun provideJournalPromptDao(database: OrielleDatabase): JournalPromptDao {
        return database.journalPromptDao()
    }
}
