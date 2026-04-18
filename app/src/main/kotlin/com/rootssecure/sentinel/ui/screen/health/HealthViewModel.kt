package com.rootssecure.sentinel.ui.screen.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import com.rootssecure.sentinel.domain.usecase.GetHeartbeatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHeartbeatHistory: GetHeartbeatHistoryUseCase,
    private val devSettings: DeveloperSettingsRepository
) : ViewModel() {

    private val tickerFlow = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000)
        }
    }

    val uiState: StateFlow<HealthUiState> =
        devSettings.isDeveloperModeEnabled.distinctUntilChanged()
            .flatMapLatest { devMode ->
                combine(
                    getHeartbeatHistory(includeMock = devMode),
                    com.rootssecure.sentinel.data.mqtt.MqttConnectionManager.isConnected,
                    com.rootssecure.sentinel.data.mqtt.MqttConnectionManager.lastError,
                    tickerFlow
                ) { history, mqttConnected, mqttError, now ->
                    if (history.isEmpty()) {
                        HealthUiState.Error("No telemetry data received yet.")
                    } else {
                        val latest = history.first()
                        val heartbeatActive = kotlin.math.abs(now - latest.recordedAt.toEpochMilli()) < 120_000
                        val isConnected = mqttConnected && heartbeatActive

                        HealthUiState.Success(
                            latestHeartbeat = latest,
                            history = history.reversed(),
                            isConnected = isConnected,
                            mqttError = mqttError
                        )
                    }
                }
            }
        .catch { e -> emit(HealthUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HealthUiState.Loading
        )
}
