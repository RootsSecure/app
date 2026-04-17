package com.rootssecure.sentinel.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import com.rootssecure.sentinel.data.mqtt.MqttLogManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToProvisioning: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = { TopBar(title = "System Settings") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section (Static Display)
            SettingsSection(title = "USER ACCOUNT") {
                val activeOwner = state.properties.firstOrNull { it.id == state.activePropertyId }?.ownerName ?: "Unknown Owner"
                ProfileControl(activeOwner, "Owner Login")
            }

            // Property Information Editor
            SettingsSection(title = "SITE MANAGEMENT (LOCAL DB)") {
                state.properties.forEach { property ->
                    key(property.id) {
                        PropertyCard(
                            property = property,
                            onSave = { updated -> 
                                viewModel.updatePropertyInfo(updated)
                                scope.launch { snackbarHostState.showSnackbar("Property details saved.") }
                            },
                            onDelete = {
                                viewModel.deleteProperty(property.id)
                                scope.launch { snackbarHostState.showSnackbar("Property removed.") }
                            },
                            onSetActive = { viewModel.setActiveProperty(property.id) },
                            isActive = property.id == state.activePropertyId
                        )
                    }
                }

                Button(
                    onClick = { viewModel.addProperty() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletContainer, contentColor = ElectricViolet),
                    shape = SentinelShapes.medium
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ADD NEW PROPERTY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Connection Section
            SettingsSection(title = "MQTT ARCHITECTURE") {
                var localHost by remember(state.mqttConfig.brokerHost) { mutableStateOf(state.mqttConfig.brokerHost) }
                var localUser by remember(state.mqttConfig.username) { mutableStateOf(state.mqttConfig.username ?: "") }
                var localPass by remember(state.mqttConfig.password) { mutableStateOf(state.mqttConfig.password ?: "") }

                Surface(
                    color = SurfaceContainer,
                    shape = SentinelShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PropertyField("Broker Host", localHost) { localHost = it }
                        PropertyField("Username", localUser) { localUser = it }
                        PropertyField("Password", localPass) { localPass = it }

                        Button(
                            onClick = { 
                                viewModel.updateMqttConfig(state.mqttConfig.copy(
                                    brokerHost = localHost,
                                    username = localUser,
                                    password = localPass
                                ))
                                scope.launch { snackbarHostState.showSnackbar("Network settings updated. Reconnecting...") }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletContainer, contentColor = ElectricViolet),
                            shape = SentinelShapes.small
                        ) {
                            Text("SAVE NETWORK SETTINGS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
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

            // Connection Diagnostic Section (Developer Only)
            if (state.isDeveloperMode) {
                val logs by MqttLogManager.logs.collectAsState()
                SettingsSection(title = "CONNECTION DIAGNOSTICS") {
                    Surface(
                        color = SurfaceBright,
                        shape = SentinelShapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Waiting for MQTT events...", color = OnSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(logs) { log ->
                                    Text(
                                        text = log,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        color = if (log.contains("ERROR")) CriticalRed else if (log.contains("Connected") || log.contains("Subscribing")) SafeGreen else OnSurface
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { MqttLogManager.clear() },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.textButtonColors(contentColor = OnSurfaceVariant),
                    ) {
                        Text("CLEAR LOGS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // App Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SENTINEL v2.0.0 (Cloud-Native)",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "End-to-End Encryption / HiveMQ Cloud",
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
        color = androidx.compose.ui.graphics.Color.Transparent,
        shape = SentinelShapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Background, VioletContainer)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = HighAmber
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
private fun PropertyCard(
    property: com.rootssecure.sentinel.data.local.entity.PropertyInfoEntity,
    onSave: (com.rootssecure.sentinel.data.local.entity.PropertyInfoEntity) -> Unit,
    onDelete: () -> Unit,
    onSetActive: () -> Unit,
    isActive: Boolean
) {
    Surface(
        color = SurfaceContainer,
        shape = SentinelShapes.medium,
        modifier = Modifier.fillMaxWidth(),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null
    ) {
        // We use remember { } without keys to prevent focus loss during database updates.
        // The LaunchedEffect ensures we seed the initial values once.
        var localName by remember { mutableStateOf(property.propertyName) }
        var localOwner by remember { mutableStateOf(property.ownerName) }
        var localAddress by remember { mutableStateOf(property.address) }
        var localMqtt by remember { mutableStateOf(property.mqttTopicId) }

        val isDirty = localName != property.propertyName ||
                      localOwner != property.ownerName ||
                      localAddress != property.address ||
                      localMqtt != property.mqttTopicId

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PropertyField("Property Name", localName) { localName = it }
            PropertyField("Owner Name", localOwner) { localOwner = it }
            PropertyField("Site Address", localAddress) { localAddress = it }
            PropertyField("MQTT Topic ID", localMqtt) { localMqtt = it }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSave(property.copy(propertyName = localName, ownerName = localOwner, address = localAddress, mqttTopicId = localMqtt)) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDirty) TealPrimary else TealPrimary.copy(alpha = 0.15f),
                        contentColor = if (isDirty) Background else TealPrimary
                    ),
                    shape = SentinelShapes.small
                ) {
                    Text("SAVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (!isActive) {
                    Button(
                        onClick = onSetActive,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceBright,
                            contentColor = OnSurface
                        ),
                        shape = SentinelShapes.small
                    ) {
                        Text("SET ACTIVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = CriticalRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Property")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Data saved locally in Secure Database",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.5f)
                )
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
