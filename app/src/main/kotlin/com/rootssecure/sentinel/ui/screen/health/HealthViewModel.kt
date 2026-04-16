package com.rootssecure.sentinel.ui.screen.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.usecase.GetHeartbeatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getHeartbeatHistory: GetHeartbeatHistoryUseCase,
    private val devSettings: DeveloperSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HealthUiState> =
        combine(
            getHeartbeatHistory(),
            devSettings.isDeveloperModeEnabled
        ) { history, devMode ->
            val filtered = if (devMode) history else history.filter { !it.isMock }
            
            if (filtered.isEmpty()) {
                HealthUiState.Error("No telemetry data received yet.")
            } else {
                HealthUiState.Success(
                    latestHeartbeat = filtered.first(),
                    history = filtered.reversed()
                )
            }
        }
        .catch { e -> emit(HealthUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HealthUiState.Loading
        )
}
