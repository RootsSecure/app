package com.rootssecure.sentinel.ui.screen.timeline

import com.rootssecure.sentinel.domain.model.AlertEvent

sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Success(val alerts: List<AlertEvent>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}
