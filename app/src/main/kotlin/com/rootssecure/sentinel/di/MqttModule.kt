package com.rootssecure.sentinel.di

import com.rootssecure.sentinel.data.mqtt.MqttConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for MQTT configuration.
 *
 * In a production app, [providesMqttConfig] would read broker host/port
 * from [androidx.datastore.preferences.core.Preferences] (set via the
 * Provisioning screen) rather than using compile-time defaults.
 */
@Module
@InstallIn(SingletonComponent::class)
object MqttModule {

    @Provides
    @Singleton
    fun providesMqttConfig(): MqttConfig = MqttConfig()  // defaults to 192.168.4.1:1883
}
