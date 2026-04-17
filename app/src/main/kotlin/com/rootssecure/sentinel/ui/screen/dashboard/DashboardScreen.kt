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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.model.PowerStatus
import com.rootssecure.sentinel.ui.common.StatusDot
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.screen.dashboard.components.CpuTempGauge
import com.rootssecure.sentinel.ui.screen.dashboard.components.MetricCard
import com.rootssecure.sentinel.ui.theme.*
import androidx.compose.material3.Surface

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        val title = "Device Status"
        
        TopBar(title = title)

        AnimatedContent(targetState = state, label = "dashboard_state") { uiState ->
            when (uiState) {
                is DashboardUiState.Loading -> DashboardLoading()
                is DashboardUiState.Error   -> DashboardError(uiState.message)
                is DashboardUiState.Success -> DashboardContent(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState.Success, viewModel: DashboardViewModel) {
    val hb = state.latestHeartbeat

    Column(
        modifier          = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        if (!state.isConnected) {
            // ── Disconnected State Focus ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "No Device Connected",
                        modifier = Modifier.size(100.dp),
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Device Connected",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // ── Editorial Header ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "System secure.",
                    style = MaterialTheme.typography.displayMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                StatusDot(
                    online = state.nodeStatus is EdgeNodeStatus.Online,
                    modifier = Modifier.size(12.dp).padding(bottom = 12.dp)
                )
            }

            // ── Primary Status Metric (Asymmetric) ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Focus: CPU Temperature with History
                Box(modifier = Modifier.weight(1.2f)) {
                    val cpuTemp by viewModel.cpuTempFlow.collectAsState()
                    val cpuHistory by viewModel.cpuHistoryFlow.collectAsState()
                    
                    CpuTempGauge(
                        currentTemp = { cpuTemp },
                        history     = { cpuHistory }
                    )
                }

                // Focus: Network Latency
                Column(
                    modifier = Modifier.weight(0.8f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val latency by viewModel.latencyFlow.collectAsState()
                    
                    MetricCard(
                        label    = "LATENCY",
                        value    = { "$latency" },
                        unit     = "ms",
                        subtitle = "4G Lora Uplink",
                        isWarning = { latency > 150 }
                    )
                    
                    val powerStatus by viewModel.powerStatusFlow.collectAsState()
                    
                    MetricCard(
                        label    = "POWER",
                        value    = { if (powerStatus is PowerStatus.DirectPower) "AC" else "BAT" },
                        unit     = "",
                        subtitle = if (powerStatus is PowerStatus.DirectPower) "Stable" else "Fallback",
                        isWarning = { powerStatus is PowerStatus.BatteryFallback }
                    )
                }
            }

            // ── Quick Access Health Snapshot ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "NODE RESOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val ramUsage by viewModel.ramUsageFlow.collectAsState()
                    val storageUsage by viewModel.storageUsageFlow.collectAsState()
                    val batteryPercent by viewModel.batteryPercentFlow.collectAsState()
                    
                    ResourceMiniCard(label = "RAM", value = { "${ramUsage.toInt()}%" }, modifier = Modifier.weight(1f))
                    ResourceMiniCard(label = "DISK", value = { "${storageUsage.toInt()}%" }, modifier = Modifier.weight(1f))
                    ResourceMiniCard(label = "BATT", value = { "$batteryPercent%" }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ResourceMiniCard(label: String, value: () -> String, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceContainer,
        shape = SentinelShapes.extraSmall,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(text = value(), style = MaterialTheme.typography.titleMedium, color = OnBackground, fontWeight = FontWeight.Bold)
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
