package com.rootssecure.sentinel.ui.screen.alertdetail

import com.rootssecure.sentinel.domain.model.AlertEvent

sealed class AlertDetailUiState {
    object Loading : AlertDetailUiState()
    data class Success(val alert: AlertEvent) : AlertDetailUiState()
    object NotFound : AlertDetailUiState()
}
