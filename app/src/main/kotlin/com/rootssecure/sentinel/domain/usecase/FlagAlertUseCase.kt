package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.repository.AlertRepository
import javax.inject.Inject

/** Use case: Mark an alert as a false alarm by its vendor event ID. */
class FlagAlertUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    suspend operator fun invoke(alertId: String) = repository.flagAsFalseAlarm(alertId)
}
