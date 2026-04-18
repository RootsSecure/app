package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.model.AlertEvent
import com.rootssecure.sentinel.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Observe the full alert feed, newest-first.
 *
 * Returns a [Flow] that emits a new list every time [MqttService] writes
 * a new alert to Room DB. The UI layer simply collects this flow.
 *
 * This is the primary use case — used by [TimelineViewModel].
 */
class GetActiveAlertsUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    operator fun invoke(includeMock: Boolean): Flow<List<AlertEvent>> = repository.observeAll(includeMock)
}
