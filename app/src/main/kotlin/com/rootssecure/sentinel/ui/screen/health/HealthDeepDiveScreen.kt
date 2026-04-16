package com.rootssecure.sentinel.ui.screen.health

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.screen.dashboard.components.CpuTempGauge
import com.rootssecure.sentinel.ui.screen.dashboard.components.MetricCard
import com.rootssecure.sentinel.ui.theme.*

@Composable
fun HealthDeepDiveScreen(viewModel: HealthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(title = "Health Monitoring")

        AnimatedContent(targetState = state, label = "health_state") { uiState ->
            when (uiState) {
                is HealthUiState.Loading -> HealthLoading()
                is HealthUiState.Success -> HealthContent(uiState)
                is HealthUiState.Error -> HealthError(uiState.message)
            }
        }
    }
}

@Composable
private fun HealthContent(state: HealthUiState.Success) {
    val hb = state.latestHeartbeat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Editorial header: Extreme scale contrast
        Column {
            Text(
                text = "SYSTEM TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = TealPrimary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Device Status: OPTIMAL",
                style = MaterialTheme.typography.displayMedium,
                color = OnBackground,
                fontWeight = FontWeight.Bold
            )
        }

        // Primary Metrics: CPU and Battery
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CpuTempGauge(
                    currentTemp = hb.cpuTempC,
                    history = state.history.map { it.cpuTempC.toFloat() }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    label = "BATTERY",
                    value = "${hb.batteryPercent}",
                    unit = "%",
                    subtitle = if (hb.batteryPercent < 20) "Critically Low" else "Power Stable",
                    isWarning = hb.batteryPercent < 20
                )
            }
        }

        // Secondary Metrics: RAM and Storage
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    label = "RAM USAGE",
                    value = hb.ramUsagePercent.toInt().toString(),
                    unit = "%",
                    subtitle = if (hb.ramUsagePercent > 85) "Memory Pressure" else "Memory Optimal",
                    isWarning = hb.ramUsagePercent > 85
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(
                    label = "STORAGE",
                    value = hb.storageUsagePercent.toInt().toString(),
                    unit = "%",
                    subtitle = if (hb.storageUsagePercent > 90) "Disk Nearly Full" else "Storage Available",
                    isWarning = hb.storageUsagePercent > 90
                )
            }
        }

        // Latency and Signal
        MetricCard(
            label = "NETWORK LATENCY",
            value = "${hb.networkLatencyMs}",
            unit = "ms",
            subtitle = "Active connection to Mosquitto MQTT",
            isWarning = hb.networkLatencyMs > 200
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HealthLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TealPrimary)
    }
}

@Composable
private fun HealthError(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Telemetry Error: $message", color = CriticalRed, style = MaterialTheme.typography.bodyMedium)
    }
}
