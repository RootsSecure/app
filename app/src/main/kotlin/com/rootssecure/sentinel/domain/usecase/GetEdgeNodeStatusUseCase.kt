package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.repository.HeartbeatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case: Derive the current connectivity status of the Edge Node
 * based on the most recent heartbeat timestamp.
 *
 * This is used by the Dashboard to show the green/red status light.
 */
class GetEdgeNodeStatusUseCase @Inject constructor(
    private val repository: HeartbeatRepository
) {
    operator fun invoke(): Flow<EdgeNodeStatus> = repository.observeLatest().map { latest ->
        when {
            latest == null -> EdgeNodeStatus.Connecting
            isRecent(latest.recordedAt.toEpochMilli()) -> EdgeNodeStatus.Online(latest.recordedAt.toEpochMilli())
            else -> EdgeNodeStatus.Stale
        }
    }

    private fun isRecent(epochMs: Long): Boolean {
        val ninetyMinutesMs = 90 * 60 * 1_000L
        return (System.currentTimeMillis() - epochMs) < ninetyMinutesMs
    }
}
