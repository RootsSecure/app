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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for [DashboardScreen].
 *
 * Derives [uiState] by combining the heartbeat history and real-time status flows.
 */
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository

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

    val uiState: StateFlow<DashboardUiState> =
        combine(
            getHeartbeatHistory(),
            getEdgeNodeStatus(),
            devSettings.isDeveloperModeEnabled.distinctUntilChanged(),
            tickerFlow
        ) { history, status, devMode, now ->
            val filteredHistory = if (devMode) history else history.filter { !it.isMock }
            val latest = filteredHistory.firstOrNull()
            
            val isConnected = latest != null && (now - latest.recordedAt.toEpochMilli() < 120_000)

            DashboardUiState.Success(
                nodeStatus       = status,
                latestHeartbeat  = latest,
                heartbeatHistory = filteredHistory.reversed(),
                isConnected      = isConnected
            ) as DashboardUiState
        }
        .catch { e ->
            emit(DashboardUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )

    private val filteredHistoryFlow = combine(
        getHeartbeatHistory(),
        devSettings.isDeveloperModeEnabled.distinctUntilChanged()
    ) { history, devMode ->
        if (devMode) history else history.filter { !it.isMock }
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

    val cpuHistoryFlow: StateFlow<List<Float>> = filteredHistoryFlow.map { list -> list.reversed().map { it.cpuTempC.toFloat() } }
        .distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
