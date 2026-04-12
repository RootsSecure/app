package com.rootssecure.sentinel.ui.screen.provisioning

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.screen.provisioning.components.DeviceListItem
import com.rootssecure.sentinel.ui.screen.provisioning.components.RadarAnimation
import com.rootssecure.sentinel.ui.screen.provisioning.components.WifiConfigForm
import com.rootssecure.sentinel.ui.theme.*

@Composable
fun ProvisioningScreen(
    viewModel: ProvisioningViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(title = "Hardware Provisioning")

        AnimatedContent(
            targetState = state,
            label = "provisioning_state",
            modifier = Modifier.fillMaxSize()
        ) { uiState ->
            when (uiState) {
                is ProvisioningUiState.Idle -> ProvisioningIdle(onStart = { viewModel.startScan() })
                is ProvisioningUiState.Scanning -> ProvisioningScanning()
                is ProvisioningUiState.DevicesFound -> ProvisioningDevicesFound(
                    devices = uiState.devices,
                    onSelect = { viewModel.selectDevice(it) }
                )
                is ProvisioningUiState.Connected -> ProvisioningConnected(
                    onProvision = { ssid, pass, broker -> viewModel.pushConfig(ssid, pass, broker) }
                )
                is ProvisioningUiState.Pushing -> ProvisioningPushing()
                is ProvisioningUiState.Success -> ProvisioningSuccess(onDone = { viewModel.reset() })
                is ProvisioningUiState.Error -> ProvisioningError(message = uiState.message, onRetry = { viewModel.startScan() })
            }
        }
    }
}

@Composable
private fun ProvisioningIdle(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup your Sentinel Node",
            style = MaterialTheme.typography.headlineSmall,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Connect to your Raspberry Pi via BLE to configure its Wi-Fi and MQTT settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = Background),
            shape = SentinelShapes.medium
        ) {
            Text("START BLE SCAN", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProvisioningScanning() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RadarAnimation()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Scanning for Sentinel devices…",
            style = MaterialTheme.typography.bodyLarge,
            color = TealPrimary
        )
        Text(
            text = "Ensure your Pi's Bluetooth is enabled",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun ProvisioningDevicesFound(
    devices: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Devices Found",
            style = MaterialTheme.typography.titleMedium,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        devices.forEach { device ->
            DeviceListItem(name = device, onClick = { onSelect(device) })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProvisioningConnected(
    onProvision: (ssid: String, pass: String, broker: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Configure Network",
            style = MaterialTheme.typography.titleLarge,
            color = OnBackground
        )
        Text(
            text = "The Pi will use these credentials to join your local network.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        WifiConfigForm(onProvisionClick = onProvision)
    }
}

@Composable
private fun ProvisioningPushing() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Writing credentials to device…",
            style = MaterialTheme.typography.bodyLarge,
            color = OnBackground
        )
        Text(
            text = "Do not close the app or move away from the Pi",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun ProvisioningSuccess(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✓ SUCCESS",
            style = MaterialTheme.typography.displaySmall,
            color = SafeGreen
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sentinel Node is now configured and connecting to the MQTT broker.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen, contentColor = Background),
            shape = SentinelShapes.medium
        ) {
            Text("FINISH", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProvisioningError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Provisioning Failed",
            style = MaterialTheme.typography.titleLarge,
            color = CriticalRed
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed, contentColor = OnBackground),
            shape = SentinelShapes.medium
        ) {
            Text("RETRY SCAN", style = MaterialTheme.typography.labelLarge)
        }
    }
}
