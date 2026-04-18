package com.rootssecure.sentinel.data.mqtt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Singleton
object MqttConnectionManager {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
        if (connected) _lastError.value = null
    }

    fun setError(error: String?) {
        _lastError.value = error
        if (error != null) _isConnected.value = false
    }
}
