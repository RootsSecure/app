package com.rootssecure.sentinel.ui.screen.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.model.Heartbeat
import com.rootssecure.sentinel.domain.model.PowerStatus
import com.rootssecure.sentinel.ui.common.StatusDot
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.screen.dashboard.components.CpuTempGauge
import com.rootssecure.sentinel.ui.screen.dashboard.components.MetricCard
import com.rootssecure.sentinel.ui.screen.dashboard.components.PowerStatusCard
import com.rootssecure.sentinel.ui.theme.Background
import com.rootssecure.sentinel.ui.theme.CriticalRed
import com.rootssecure.sentinel.ui.theme.OnBackground
import com.rootssecure.sentinel.ui.theme.SafeGreen
import com.rootssecure.sentinel.ui.theme.TealPrimary

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(title = "Edge Node Monitor")

        AnimatedContent(targetState = state, label = "dashboard_state") { uiState ->
            when (uiState) {
                is DashboardUiState.Loading -> DashboardLoading()
                is DashboardUiState.Error   -> DashboardError(uiState.message)
                is DashboardUiState.Success -> DashboardContent(uiState)
            }
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState.Success) {
    Column(
        modifier          = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Status row ───────────────────────────────────────────────────────
        Row(
            modifier       = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(
                online = state.nodeStatus is EdgeNodeStatus.Online,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text  = when (state.nodeStatus) {
                    is EdgeNodeStatus.Online     -> "Edge Node Online"
                    is EdgeNodeStatus.Stale      -> "No Recent Heartbeat"
                    is EdgeNodeStatus.Offline    -> "Edge Node Offline"
                    is EdgeNodeStatus.Connecting -> "Connecting…"
                },
                style = MaterialTheme.typography.labelLarge,
                color = when (state.nodeStatus) {
                    is EdgeNodeStatus.Online     -> SafeGreen
                    is EdgeNodeStatus.Connecting -> TealPrimary
                    else                         -> CriticalRed
                }
            )
        }

        // ── CPU Temperature ──────────────────────────────────────────────────
        val heartbeat = state.latestHeartbeat
        if (heartbeat != null) {
            CpuTempGauge(
                currentTemp = heartbeat.cpuTempC,
                history     = state.heartbeatHistory.map { it.cpuTempC.toFloat() }
            )

            MetricCard(
                label    = "Network Latency",
                value    = "${heartbeat.networkLatencyMs}",
                unit     = "ms",
                subtitle = if (heartbeat.networkLatencyMs > 150) "4G Signal Degraded" else "4G Signal Strong",
                isWarning = heartbeat.networkLatencyMs > 150
            )

            PowerStatusCard(
                powerStatus = heartbeat.powerStatus
            )
        } else {
            Text(
                text  = "Waiting for first heartbeat from Pi…",
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DashboardLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text  = "Connecting to MQTT broker…",
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun DashboardError(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text      = "Error: $message",
            style     = MaterialTheme.typography.bodyMedium,
            color     = CriticalRed,
            fontWeight = FontWeight.Bold
        )
    }
}
