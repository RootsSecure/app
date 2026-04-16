package com.rootssecure.sentinel.ui.screen.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.usecase.FlagAlertUseCase
import com.rootssecure.sentinel.domain.usecase.GetActiveAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import kotlinx.coroutines.flow.*

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getActiveAlerts: GetActiveAlertsUseCase,
    private val flagAlert: FlagAlertUseCase,
    private val devSettings: DeveloperSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> =
        combine(
            getActiveAlerts(),
            devSettings.isDeveloperModeEnabled.distinctUntilChanged()
        ) { alerts, devMode ->
            val filtered = if (devMode) alerts else alerts.filter { !it.isMock }
            TimelineUiState.Success(filtered) as TimelineUiState
        }
            .catch { emit(TimelineUiState.Error(it.message ?: "Unknown error")) }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = TimelineUiState.Loading
            )

    fun flagAsFalseAlarm(alertId: String) {
        viewModelScope.launch { flagAlert(alertId) }
    }
}
