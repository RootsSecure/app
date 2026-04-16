package com.rootssecure.sentinel.data.mqtt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A simple in-memory log manager for diagnostic purposes.
 * Stores up to 200 of the most recent MQTT events.
 */
object MqttLogManager {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())

    fun log(message: String) {
        val time = formatter.format(Instant.now())
        val newLog = "[$time] $message"
        _logs.value = listOf(newLog) + _logs.value.take(199)
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
