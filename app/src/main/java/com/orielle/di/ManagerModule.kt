package com.orielle.di

import com.orielle.data.manager.BiometricAuthManagerImpl
import com.orielle.data.manager.BillingManagerImpl
import com.orielle.data.manager.SessionManagerImpl
import com.orielle.data.manager.SyncManagerImpl
import com.orielle.domain.manager.BiometricAuthManager
import com.orielle.domain.manager.BillingManager
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

    @Provides
    @Singleton
    fun provideSessionManager(impl: SessionManagerImpl): SessionManager = impl

    @Provides
    @Singleton
    fun provideBiometricAuthManager(impl: BiometricAuthManagerImpl): BiometricAuthManager = impl

    @Provides
    @Singleton
    fun provideBillingManager(impl: BillingManagerImpl): BillingManager = impl

    @Provides
    @Singleton
    fun provideSyncManager(impl: SyncManagerImpl): SyncManager = impl
}