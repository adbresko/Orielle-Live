package com.orielle.di

import com.orielle.data.manager.SessionManagerImpl
import com.orielle.domain.manager.SessionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.orielle.data.manager.BiometricAuthManagerImpl
import com.orielle.domain.manager.BiometricAuthManager

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    @Singleton
    abstract fun bindSessionManager(
        sessionManagerImpl: SessionManagerImpl,
    ): SessionManager // Hilt now knows to provide SessionManagerImpl when SessionManager is requested

    @Binds
    @Singleton
    abstract fun bindBiometricAuthManager(
        biometricAuthManagerImpl: BiometricAuthManagerImpl,
    ): BiometricAuthManager
}