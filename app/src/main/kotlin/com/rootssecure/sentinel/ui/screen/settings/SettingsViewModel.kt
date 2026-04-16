package com.rootssecure.sentinel.ui.screen.settings

import androidx.lifecycle.ViewModel
import com.rootssecure.sentinel.data.mqtt.MqttConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.rootssecure.sentinel.data.local.entity.PropertyDao
import com.rootssecure.sentinel.data.local.entity.PropertyInfoEntity

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mqttConfig: MqttConfig,
    private val devSettings: DeveloperSettingsRepository,
    private val propertyDao: PropertyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(mqttConfig = mqttConfig))
    
    val uiState: StateFlow<SettingsUiState> = combine(
        _uiState,
        devSettings.isDeveloperModeEnabled,
        propertyDao.getPropertyInfo()
    ) { state, devMode, property ->
        state.copy(
            isDeveloperMode = devMode,
            propertyInfo    = property ?: PropertyInfoEntity(
                propertyName = "My Property",
                ownerName    = "Owner Name",
                address      = "Property Address"
            )
        )
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        SettingsUiState(mqttConfig = mqttConfig)
    )

    fun toggleDeveloperMode(enabled: Boolean) {
        viewModelScope.launch {
            devSettings.setDeveloperMode(enabled)
        }
    }

    fun updatePropertyInfo(name: String, owner: String, address: String) {
        viewModelScope.launch {
            propertyDao.updatePropertyInfo(
                PropertyInfoEntity(
                    propertyName = name,
                    ownerName    = owner,
                    address      = address
                )
            )
        }
    }
}

data class SettingsUiState(
    val mqttConfig: MqttConfig,
    val isDeveloperMode: Boolean = false,
    val propertyInfo: PropertyInfoEntity = PropertyInfoEntity(
        propertyName = "My Property",
        ownerName    = "Owner Name",
        address      = "Property Address"
    )
)
