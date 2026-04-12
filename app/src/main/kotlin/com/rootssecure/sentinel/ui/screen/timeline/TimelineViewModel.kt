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

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getActiveAlerts: GetActiveAlertsUseCase,
    private val flagAlert: FlagAlertUseCase
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> =
        getActiveAlerts()
            .map { TimelineUiState.Success(it) as TimelineUiState }
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
