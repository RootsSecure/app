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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for [DashboardScreen].
 *
 * Derives [uiState] by combining the heartbeat history and real-time status flows.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getHeartbeatHistory: GetHeartbeatHistoryUseCase,
    private val getEdgeNodeStatus: GetEdgeNodeStatusUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(
            getHeartbeatHistory(),
            getEdgeNodeStatus()
        ) { history, status ->
            DashboardUiState.Success(
                nodeStatus       = status,
                latestHeartbeat  = history.firstOrNull(),
                heartbeatHistory = history.reversed()   // oldest first for charts
            )
        }
        .catch { e ->
            emit(DashboardUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )
}
