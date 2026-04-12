package com.rootssecure.sentinel.ui.screen.dashboard

import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.model.Heartbeat

/** Sealed UI state for the Dashboard screen. */
sealed class DashboardUiState {
    object Loading : DashboardUiState()

    data class Success(
        val nodeStatus: EdgeNodeStatus,
        val latestHeartbeat: Heartbeat?,
        val heartbeatHistory: List<Heartbeat>  // chronological, oldest first, for charts
    ) : DashboardUiState()

    data class Error(val message: String) : DashboardUiState()
}
