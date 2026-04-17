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
    private val devSettings: DeveloperSettingsRepository,
    private val propertyDao: PropertyDao,
    private val configRepo: com.rootssecure.sentinel.domain.repository.MqttConfigRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        devSettings.isDeveloperModeEnabled.distinctUntilChanged(),
        propertyDao.getAllProperties(),
        configRepo.config
    ) { devMode, properties, config ->
        SettingsUiState(
            isDeveloperMode = devMode,
            properties = properties,
            mqttConfig = config,
            activePropertyId = properties.firstOrNull { it.isActive }?.id ?: properties.firstOrNull()?.id
        )
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        SettingsUiState(mqttConfig = MqttConfig())
    )

    fun toggleDeveloperMode(enabled: Boolean) {
        viewModelScope.launch {
            devSettings.setDeveloperMode(enabled)
        }
    }

    fun addProperty() {
        viewModelScope.launch {
            propertyDao.updatePropertyInfo(
                PropertyInfoEntity(
                    propertyName = "New Property",
                    ownerName = "Owner",
                    address = "Address",
                    mqttTopicId = "telemetry/new_property",
                    isActive = false
                )
            )
        }
    }

    fun setActiveProperty(id: Int) {
        viewModelScope.launch {
            propertyDao.toggleActiveProperty(id)
        }
    }

    fun updatePropertyInfo(property: PropertyInfoEntity) {
        viewModelScope.launch {
            propertyDao.updatePropertyInfo(property)
        }
    }

    fun deleteProperty(id: Int) {
        viewModelScope.launch {
            propertyDao.deletePropertyById(id)
        }
    }

    fun updateMqttConfig(config: MqttConfig) {
        viewModelScope.launch {
            configRepo.updateConfig(config)
        }
    }
}

data class SettingsUiState(
    val mqttConfig: MqttConfig,
    val isDeveloperMode: Boolean = false,
    val properties: List<PropertyInfoEntity> = emptyList(),
    val activePropertyId: Int? = null
)
