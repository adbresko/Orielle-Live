package com.orielle.di

import android.content.Context
import androidx.room.Room
import com.orielle.data.local.OrielleDatabase
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.MoodTagDao
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
    fun provideOrielleDatabase(@ApplicationContext context: Context): OrielleDatabase {
        return Room.databaseBuilder(
            context,
            OrielleDatabase::class.java,
            "orielle_database"
        )
            .fallbackToDestructiveMigration() // Use this during development
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

    /**
     * Provides a singleton instance of the MoodCheckInDao.
     * @param database The OrielleDatabase instance provided by Hilt.
     * @return An instance of MoodCheckInDao.
     */
    @Provides
    @Singleton
    fun provideMoodCheckInDao(database: OrielleDatabase): MoodCheckInDao {
        return database.moodCheckInDao()
    }

    /**
     * Provides a singleton instance of the ChatMessageDao.
     * @param database The OrielleDatabase instance provided by Hilt.
     * @return An instance of ChatMessageDao.
     */
    @Provides
    @Singleton
    fun provideChatMessageDao(database: OrielleDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    /**
     * Provides a singleton instance of the MoodTagDao.
     * @param database The OrielleDatabase instance provided by Hilt.
     * @return An instance of MoodTagDao.
     */
    @Provides
    @Singleton
    fun provideMoodTagDao(database: OrielleDatabase): MoodTagDao {
        return database.moodTagDao()
    }
}
