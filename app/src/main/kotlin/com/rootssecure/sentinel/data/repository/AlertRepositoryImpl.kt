package com.rootssecure.sentinel.data.repository

import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import com.rootssecure.sentinel.domain.model.AlertEvent
import com.rootssecure.sentinel.domain.model.AlertSeverity
import com.rootssecure.sentinel.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Implements [AlertRepository] using Room as the single source of truth.
 *
 * Room is populated by [MqttService] directly — this repository only reads
 * and maps. No network calls happen here; the MQTT service handles ingestion.
 */
class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertEventDao
) : AlertRepository {

    override fun observeAll(): Flow<List<AlertEvent>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCritical(): Flow<List<AlertEvent>> =
        dao.observeCritical().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): AlertEvent? =
        dao.getById(id)?.toDomain()

    override suspend fun flagAsFalseAlarm(id: String) =
        dao.flagAsFalseAlarm(id)

    // ── Mapper ────────────────────────────────────────────────────────────────

    private fun AlertEventEntity.toDomain() = AlertEvent(
        id         = vendorEventId,
        title      = edgeEventType.replace("_", " "),
        reason     = reason,
        severity   = AlertSeverity.fromString(logicLevel),
        occurredAt = Instant.parse(occurredAt),
        imageUrl   = mediaRef,
        isFlagged  = isFlagged,
        isMock     = isMock
    )
}
