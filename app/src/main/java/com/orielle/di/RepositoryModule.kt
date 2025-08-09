package com.orielle.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.TagDao
import com.orielle.domain.repository.AuthRepository
import com.orielle.data.repository.AuthRepositoryImpl
import com.orielle.domain.repository.JournalRepository
import com.orielle.data.repository.JournalRepositoryImpl
import com.orielle.domain.repository.MoodCheckInRepository
import com.orielle.data.repository.MoodCheckInRepositoryImpl
import com.orielle.domain.repository.ChatRepository
import com.orielle.data.repository.ChatRepositoryImpl
import com.orielle.domain.repository.TagRepository
import com.orielle.data.repository.TagRepositoryImpl
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        syncManager: SyncManager
    ): AuthRepository = AuthRepositoryImpl(auth, db, syncManager)

    // CORRECTED: This provider now includes all necessary dependencies.
    @Provides
    @Singleton
    fun provideJournalRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        journalDao: JournalDao, // The previously missing DAO
        sessionManager: SessionManager // The new session manager
    ): JournalRepository = JournalRepositoryImpl(
        auth = auth,
        firestore = firestore,
        journalDao = journalDao,
        sessionManager = sessionManager
    )

    @Provides
    @Singleton
    fun provideMoodCheckInRepository(
        moodCheckInDao: MoodCheckInDao,
        firestore: FirebaseFirestore,
        sessionManager: SessionManager
    ): MoodCheckInRepository = MoodCheckInRepositoryImpl(
        moodCheckInDao = moodCheckInDao,
        firestore = firestore,
        sessionManager = sessionManager
    )

    @Provides
    @Singleton
    fun provideChatRepository(
        conversationDao: ChatConversationDao,
        messageDao: ChatMessageDao,
        firestore: FirebaseFirestore,
        sessionManager: SessionManager
    ): ChatRepository = ChatRepositoryImpl(
        conversationDao = conversationDao,
        messageDao = messageDao,
        firestore = firestore,
        sessionManager = sessionManager
    )

    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao,
        firestore: FirebaseFirestore,
        sessionManager: SessionManager
    ): TagRepository = TagRepositoryImpl(
        tagDao = tagDao,
        firestore = firestore,
        sessionManager = sessionManager
    )
}