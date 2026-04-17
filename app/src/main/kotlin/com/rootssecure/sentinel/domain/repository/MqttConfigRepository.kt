package com.rootssecure.sentinel.domain.repository

import com.rootssecure.sentinel.data.mqtt.MqttConfig
import kotlinx.coroutines.flow.Flow

interface MqttConfigRepository {
    val config: Flow<MqttConfig>
    suspend fun updateConfig(config: MqttConfig)
}
