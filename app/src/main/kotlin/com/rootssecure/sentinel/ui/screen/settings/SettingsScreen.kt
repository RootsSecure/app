package com.rootssecure.sentinel.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToProvisioning: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(title = "System Settings")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section (Static Display)
            SettingsSection(title = "USER ACCOUNT") {
                ProfileControl(state.propertyInfo.ownerName, "Owner Login")
            }

            // Property Information Editor
            SettingsSection(title = "SITE MANAGEMENT (LOCAL DB)") {
                Surface(
                    color = SurfaceContainer,
                    shape = SentinelShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PropertyField(
                            label = "Property Name",
                            value = state.propertyInfo.propertyName,
                            onValueChange = { viewModel.updatePropertyInfo(it, state.propertyInfo.ownerName, state.propertyInfo.address) }
                        )
                        PropertyField(
                            label = "Owner Name",
                            value = state.propertyInfo.ownerName,
                            onValueChange = { viewModel.updatePropertyInfo(state.propertyInfo.propertyName, it, state.propertyInfo.address) }
                        )
                        PropertyField(
                            label = "Site Address",
                            value = state.propertyInfo.address,
                            onValueChange = { viewModel.updatePropertyInfo(state.propertyInfo.propertyName, state.propertyInfo.ownerName, it) }
                        )
                        
                        Text(
                            text = "Data saved locally in RootsSecure Database",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Connection Section
            SettingsSection(title = "MQTT ARCHITECTURE") {
                ConfigItem(
                    icon = Icons.Default.Dns,
                    label = "Broker Host",
                    value = state.mqttConfig.brokerHost
                )
                ConfigItem(
                    icon = Icons.Default.Numbers,
                    label = "Broker Port",
                    value = state.mqttConfig.brokerPort.toString()
                )
                ConfigItem(
                    icon = Icons.Default.Label,
                    label = "Client ID",
                    value = state.mqttConfig.clientId
                )
            }

            // Hardware Action
            SettingsSection(title = "HARDWARE MANAGEMENT") {
                Button(
                    onClick = onNavigateToProvisioning,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Background
                    ),
                    shape = SentinelShapes.medium
                ) {
                    Icon(Icons.Default.SettingsBluetooth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("SETUP NEW DEVICE", fontWeight = FontWeight.Bold)
                }
            }

            // Developer Mode Section
            SettingsSection(title = "DEBUG & TESTING") {
                Surface(
                    color = SurfaceContainer,
                    shape = SentinelShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = OnSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(text = "Developer Mode", style = MaterialTheme.typography.titleSmall, color = OnBackground)
                                Text(
                                    text = "Simulate Raspberry Pi nodes & mock telemetry",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Switch(
                            checked = state.isDeveloperMode,
                            onCheckedChange = { viewModel.toggleDeveloperMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TealPrimary,
                                checkedTrackColor = TealPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // App Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROOTSSECURE v1.1.0 (Command Center)",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "Privacy-First / Local-First architecture",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun ProfileControl(name: String, role: String) {
    Surface(
        color = SurfaceContainer,
        shape = SentinelShapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TealPrimary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = OnBackground)
                Text(text = role, style = MaterialTheme.typography.bodySmall, color = OnSurface)
            }
        }
    }
}

@Composable
private fun PropertyField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnBackground),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f),
            focusedLabelColor = TealPrimary,
            unfocusedLabelColor = OnSurfaceVariant
        ),
        shape = SentinelShapes.small,
        singleLine = true
    )
}

@Composable
private fun ConfigItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = OnBackground, fontWeight = FontWeight.SemiBold)
        }
    }
}
