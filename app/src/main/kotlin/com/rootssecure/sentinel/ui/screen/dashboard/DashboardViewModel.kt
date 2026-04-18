package com.rootssecure.sentinel.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.usecase.GetEdgeNodeStatusUseCase
import com.rootssecure.sentinel.domain.usecase.GetHeartbeatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for [DashboardScreen].
 *
 * Derives [uiState] by combining the heartbeat history and real-time status flows.
 */
import com.rootssecure.sentinel.domain.model.Heartbeat
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import java.time.Instant
import kotlin.math.abs

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getHeartbeatHistory: GetHeartbeatHistoryUseCase,
    private val getEdgeNodeStatus: GetEdgeNodeStatusUseCase,
    private val devSettings: DeveloperSettingsRepository
) : ViewModel() {

    private val tickerFlow = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = devSettings.isDeveloperModeEnabled.distinctUntilChanged()
        .flatMapLatest { devMode ->
            combine(
                getHeartbeatHistory(includeMock = devMode),
                getEdgeNodeStatus(),
                com.rootssecure.sentinel.data.mqtt.MqttConnectionManager.isConnected,
                com.rootssecure.sentinel.data.mqtt.MqttConnectionManager.lastError,
                tickerFlow
            ) { h, s, mc, me, n ->
                val history = h as List<Heartbeat>
                val status = s as EdgeNodeStatus
                val mqttConnected = mc as Boolean
                val mqttError = me as String?
                val now = n as Long

                val latest = history.firstOrNull()
                
                val heartbeatActive = latest != null && abs(now - latest.recordedAt.toEpochMilli()) < 120_000
                val isConnected = mqttConnected && heartbeatActive

                DashboardUiState.Success(
                    nodeStatus       = status,
                    latestHeartbeat  = latest,
                    heartbeatHistory = history.reversed(),
                    isConnected      = isConnected,
                    mqttError        = mqttError
                ) as DashboardUiState
            }
        }
        .catch { e ->
            emit(DashboardUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val filteredHistoryFlow = devSettings.isDeveloperModeEnabled.distinctUntilChanged()
        .flatMapLatest { devMode ->
            getHeartbeatHistory(includeMock = devMode)
        }

    val cpuTempFlow: StateFlow<Double> = filteredHistoryFlow.map { it.firstOrNull()?.cpuTempC ?: 0.0 }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val ramUsageFlow: StateFlow<Double> = filteredHistoryFlow.map { it.firstOrNull()?.ramUsagePercent ?: 0.0 }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val batteryPercentFlow: StateFlow<Int> = filteredHistoryFlow.map { it.firstOrNull()?.batteryPercent ?: 0 }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val latencyFlow: StateFlow<Int> = filteredHistoryFlow.map { it.firstOrNull()?.networkLatencyMs ?: 0 }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
        
    val storageUsageFlow: StateFlow<Double> = filteredHistoryFlow.map { it.firstOrNull()?.storageUsagePercent ?: 0.0 }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val powerStatusFlow: StateFlow<com.rootssecure.sentinel.domain.model.PowerStatus> = filteredHistoryFlow.map { it.firstOrNull()?.powerStatus ?: com.rootssecure.sentinel.domain.model.PowerStatus.DirectPower }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.rootssecure.sentinel.domain.model.PowerStatus.DirectPower)

    val cpuHistoryFlow: StateFlow<List<Float>> = filteredHistoryFlow.map { list -> list.reversed().map { it.cpuTempC.toFloat() } }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
