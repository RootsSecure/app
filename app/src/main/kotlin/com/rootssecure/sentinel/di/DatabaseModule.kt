package com.rootssecure.sentinel.di

import android.content.Context
import androidx.room.Room
import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.PropertyDao
import com.rootssecure.sentinel.data.local.db.SentinelDatabase
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
    fun provideSentinelDatabase(@ApplicationContext context: Context): SentinelDatabase =
        Room.databaseBuilder(context, SentinelDatabase::class.java, "sentinel.db")
            .fallbackToDestructiveMigration()   // switch to Migrations before v1 release
            .build()

    @Provides
    fun provideAlertEventDao(db: SentinelDatabase): AlertEventDao = db.alertEventDao()

    @Provides
    fun provideHeartbeatDao(db: SentinelDatabase): HeartbeatDao = db.heartbeatDao()

    @Provides
    fun providePropertyDao(db: SentinelDatabase): PropertyDao = db.propertyDao()
}
