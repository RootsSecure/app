package com.rootssecure.sentinel.domain.repository

import com.rootssecure.sentinel.domain.model.Heartbeat
import kotlinx.coroutines.flow.Flow

interface HeartbeatRepository {
    /** Observe the last 24 heartbeat snapshots for chart rendering. */
    fun observeLast24(): Flow<List<Heartbeat>>

    /** Observe only the most recent heartbeat for the status card. */
    fun observeLatest(): Flow<Heartbeat?>
}
