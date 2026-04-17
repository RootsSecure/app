package com.rootssecure.sentinel.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rootssecure.sentinel.data.mqtt.MqttConfig
import com.rootssecure.sentinel.domain.repository.MqttConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mqttDataStore: DataStore<Preferences> by preferencesDataStore(name = "mqtt_config")

@Singleton
class MqttConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MqttConfigRepository {

    private val KEY_HOST = stringPreferencesKey("broker_host")
    private val KEY_PORT = intPreferencesKey("broker_port")
    private val KEY_USER = stringPreferencesKey("username")
    private val KEY_PASS = stringPreferencesKey("password")
    private val KEY_TLS  = booleanPreferencesKey("use_tls")

    override val config: Flow<MqttConfig> = context.mqttDataStore.data.map { prefs ->
        MqttConfig(
            brokerHost = prefs[KEY_HOST] ?: "broker.hivemq.cloud",
            brokerPort = prefs[KEY_PORT] ?: 8883,
            username = prefs[KEY_USER] ?: "sentinel_user",
            password = prefs[KEY_PASS] ?: "secure_password_123",
            useTls = prefs[KEY_TLS] ?: true
        )
    }

    override suspend fun updateConfig(config: MqttConfig) {
        context.mqttDataStore.edit { prefs ->
            prefs[KEY_HOST] = config.brokerHost
            prefs[KEY_PORT] = config.brokerPort
            config.username?.let { prefs[KEY_USER] = it }
            config.password?.let { prefs[KEY_PASS] = it }
            prefs[KEY_TLS] = config.useTls
        }
    }
}
