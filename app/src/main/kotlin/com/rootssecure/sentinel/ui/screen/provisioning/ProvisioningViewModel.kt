package com.rootssecure.sentinel.ui.screen.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.data.mqtt.MqttConfig
import com.rootssecure.sentinel.domain.repository.MqttConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the provisioning flow.
 *
 * Currently uses simulated delays to represent BLE scanning and credential
 * pushing. Replace [simulateScan] and [pushConfig] with real
 * [BleManager] calls when the BLE stack is integrated.
 */
@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    private val configRepo: MqttConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProvisioningUiState>(ProvisioningUiState.Idle)
    val uiState: StateFlow<ProvisioningUiState> = _uiState

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = ProvisioningUiState.Scanning
            delay(3_000)   // replace with BleManager.scanForDevices()
            _uiState.value = ProvisioningUiState.DevicesFound(listOf("NRI-Pi-001"))
        }
    }

    fun selectDevice(deviceName: String) {
        viewModelScope.launch {
            delay(1_000)   // replace with BleManager.connect()
            _uiState.value = ProvisioningUiState.Connected
        }
    }

    fun pushConfig(ssid: String, password: String, brokerIp: String) {
        viewModelScope.launch {
            _uiState.value = ProvisioningUiState.Pushing
            delay(2_500)   // replace with BleManager.connectAndProvision()
            
            val current = configRepo.config.first()
            val newConfig = current.copy(brokerHost = brokerIp)
            configRepo.updateConfig(newConfig)
            
            _uiState.value = ProvisioningUiState.Success(newConfig)
        }
    }

    fun reset() {
        _uiState.value = ProvisioningUiState.Idle
    }
}
