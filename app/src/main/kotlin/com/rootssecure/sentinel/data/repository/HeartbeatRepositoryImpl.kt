package com.rootssecure.sentinel.data.repository

import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity
import com.rootssecure.sentinel.domain.model.Heartbeat
import com.rootssecure.sentinel.domain.model.PowerStatus
import com.rootssecure.sentinel.domain.repository.HeartbeatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class HeartbeatRepositoryImpl @Inject constructor(
    private val dao: HeartbeatDao
) : HeartbeatRepository {

    override fun observeLast24(includeMock: Boolean): Flow<List<Heartbeat>> =
        dao.observeLast24(includeMock).map { list -> list.map { it.toDomain() } }

    override fun observeLatest(includeMock: Boolean): Flow<Heartbeat?> =
        dao.observeLatest(includeMock).map { it?.toDomain() }

    private fun HeartbeatEntity.toDomain() = Heartbeat(
        cpuTempC         = cpuTempC,
        networkLatencyMs = networkLatencyMs,
        powerStatus      = PowerStatus.fromString(powerStatus),
        ramUsagePercent   = ramUsagePercent,
        storageUsagePercent = storageUsagePercent,
        batteryPercent    = batteryPercent,
        isMock           = isMock,
        recordedAt       = Instant.ofEpochMilli(recordedAt)
    )
}
