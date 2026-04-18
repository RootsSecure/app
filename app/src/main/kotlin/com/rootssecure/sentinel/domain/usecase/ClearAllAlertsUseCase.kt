package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.repository.AlertRepository
import javax.inject.Inject

class ClearAllAlertsUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    suspend operator fun invoke() = repository.clearAllAlerts()
}
