package com.rootssecure.nriplotsentinel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rootssecure.nriplotsentinel.api.DeviceStatusResponse
import com.rootssecure.nriplotsentinel.repository.SentinelRepository
import kotlinx.coroutines.launch

data class DashboardUiState(
    val deviceName: String = "Raspberry Pi Gateway",
    val isOnline: Boolean = false,
    val lastHeartbeat: String = "Unknown",
    val networkStatus: String = "Unknown",
    val batteryLabel: String = "Battery N/A"
)

class DashboardViewModel(
    private val repository: SentinelRepository
) : ViewModel() {

    private val _dashboardState = MutableLiveData(DashboardUiState())
    val dashboardState: LiveData<DashboardUiState> = _dashboardState

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repository.fetchDeviceStatus() }
                .onSuccess { _dashboardState.value = it.toUiState() }
                .onFailure { _error.value = it.message ?: "Unable to load device status." }
            _loading.value = false
        }
    }

    private fun DeviceStatusResponse.toUiState(): DashboardUiState {
        val normalizedStatus = status.trim().lowercase()
        return DashboardUiState(
            deviceName = deviceName.ifBlank { "Raspberry Pi Gateway" },
            isOnline = normalizedStatus == "online",
            lastHeartbeat = lastHeartbeatTime.ifBlank { "Unknown" },
            networkStatus = networkStatus.replaceFirstChar { it.uppercase() },
            batteryLabel = batteryLevel?.let { "Battery $it%" } ?: "Battery N/A"
        )
    }
}

class DashboardViewModelFactory(
    private val repository: SentinelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
