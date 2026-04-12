package com.rootssecure.sentinel.ui.screen.provisioning

import com.rootssecure.sentinel.data.mqtt.MqttConfig

sealed class ProvisioningUiState {
    object Idle       : ProvisioningUiState()   // initial — show "Start Scan" button
    object Scanning   : ProvisioningUiState()   // BLE scan in progress
    data class DevicesFound(val devices: List<String>) : ProvisioningUiState()
    object Connected  : ProvisioningUiState()   // Pi found — show config form
    object Pushing    : ProvisioningUiState()   // writing credentials to Pi
    data class Success(val config: MqttConfig)  : ProvisioningUiState()
    data class Error(val message: String)       : ProvisioningUiState()
}
