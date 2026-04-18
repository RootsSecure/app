package com.rootssecure.sentinel.ui.screen.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import com.rootssecure.sentinel.domain.usecase.FlagAlertUseCase
import com.rootssecure.sentinel.domain.usecase.GetActiveAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getActiveAlerts: GetActiveAlertsUseCase,
    private val flagAlert: FlagAlertUseCase,
    private val clearAllAlertsUseCase: com.rootssecure.sentinel.domain.usecase.ClearAllAlertsUseCase,
    private val devSettings: DeveloperSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> =
        devSettings.isDeveloperModeEnabled.distinctUntilChanged()
            .flatMapLatest { devMode ->
                getActiveAlerts(includeMock = devMode).map { alerts ->
                    TimelineUiState.Success(alerts) as TimelineUiState
                }
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

    fun clearAllAlerts() {
        viewModelScope.launch { clearAllAlertsUseCase() }
    }
}
