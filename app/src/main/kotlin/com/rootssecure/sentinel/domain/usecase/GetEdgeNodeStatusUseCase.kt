package com.rootssecure.sentinel.domain.usecase

import com.rootssecure.sentinel.domain.model.EdgeNodeStatus
import com.rootssecure.sentinel.domain.repository.HeartbeatRepository
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Derive the current connectivity status of the Edge Node
 * based on the most recent heartbeat timestamp.
 *
 * This is used by the Dashboard to show the green/red status light.
 */
class GetEdgeNodeStatusUseCase @Inject constructor(
    private val repository: HeartbeatRepository,
    private val devSettings: DeveloperSettingsRepository
) {
    operator fun invoke(): Flow<EdgeNodeStatus> = combine(
        repository.observeLast24(),
        devSettings.isDeveloperModeEnabled.distinctUntilChanged()
    ) { history, devMode ->
        val filtered = if (devMode) history else history.filter { !it.isMock }
        val latest = filtered.firstOrNull()
        
        val status: EdgeNodeStatus = when {
            latest == null -> EdgeNodeStatus.Connecting
            isRecent(latest.recordedAt.toEpochMilli()) -> EdgeNodeStatus.Online(latest.recordedAt.toEpochMilli())
            else -> EdgeNodeStatus.Stale
        }
        status
    }

    private fun isRecent(epochMs: Long): Boolean {
        val ninetyMinutesMs = 90 * 60 * 1_000L
        return (System.currentTimeMillis() - epochMs) < ninetyMinutesMs
    }
}
