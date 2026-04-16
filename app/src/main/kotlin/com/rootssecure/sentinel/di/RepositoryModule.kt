package com.rootssecure.sentinel.di

import com.rootssecure.sentinel.data.repository.AlertRepositoryImpl
import com.rootssecure.sentinel.data.repository.DeveloperSettingsRepositoryImpl
import com.rootssecure.sentinel.data.repository.HeartbeatRepositoryImpl
import com.rootssecure.sentinel.domain.repository.AlertRepository
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import com.rootssecure.sentinel.domain.repository.HeartbeatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds domain repository interfaces to their data-layer implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    @Singleton
    abstract fun bindHeartbeatRepository(impl: HeartbeatRepositoryImpl): HeartbeatRepository

    @Binds
    @Singleton
    abstract fun bindDeveloperSettingsRepository(impl: DeveloperSettingsRepositoryImpl): DeveloperSettingsRepository
}
