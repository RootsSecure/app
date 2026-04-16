package com.rootssecure.sentinel.ui.screen.health

import com.rootssecure.sentinel.domain.model.Heartbeat

sealed class HealthUiState {
    object Loading : HealthUiState()
    data class Success(
        val latestHeartbeat: Heartbeat,
        val history: List<Heartbeat>
    ) : HealthUiState()
    data class Error(val message: String) : HealthUiState()
}
