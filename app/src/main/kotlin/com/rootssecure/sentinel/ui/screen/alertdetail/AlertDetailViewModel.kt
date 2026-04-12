package com.rootssecure.sentinel.ui.screen.alertdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootssecure.sentinel.domain.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle["alertId"])

    private val _uiState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val uiState: StateFlow<AlertDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            val alert = alertRepository.getById(alertId)
            _uiState.value = if (alert != null) {
                AlertDetailUiState.Success(alert)
            } else {
                AlertDetailUiState.NotFound
            }
        }
    }
}
