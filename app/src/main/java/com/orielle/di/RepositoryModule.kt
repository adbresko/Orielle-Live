package com.orielle.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.data.local.dao.JournalDao
import com.orielle.domain.repository.AuthRepository
import com.orielle.data.repository.AuthRepositoryImpl
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.repository.JournalRepositoryImpl
import com.orielle.domain.manager.SessionManager
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
        db: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, db)

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
}