package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.model.Heartbeat
import com.rootssecure.sentinel.domain.repository.HeartbeatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Use case: Observe the 24-hour heartbeat history for telemetry charts. */
class GetHeartbeatHistoryUseCase @Inject constructor(
    private val repository: HeartbeatRepository
) {
    operator fun invoke(): Flow<List<Heartbeat>> = repository.observeLast24()
}
