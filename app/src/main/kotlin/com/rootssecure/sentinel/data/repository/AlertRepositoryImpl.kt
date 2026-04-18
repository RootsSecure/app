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

    override fun observeAll(includeMock: Boolean): Flow<List<AlertEvent>> =
        dao.observeAll(includeMock).map { entities -> entities.map { it.toDomain() } }

    override fun observeCritical(includeMock: Boolean): Flow<List<AlertEvent>> =
        dao.observeCritical(includeMock).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): AlertEvent? =
        dao.getById(id)?.toDomain()

    override suspend fun flagAsFalseAlarm(id: String) =
        dao.flagAsFalseAlarm(id)

    override suspend fun clearAllAlerts() {
        val allAlerts = dao.getAllAlerts()
        allAlerts.forEach { alert ->
            alert.mediaRefs.forEach { ref ->
                if (ref.startsWith("file://")) {
                    try {
                        java.io.File(java.net.URI(ref)).delete()
                    } catch (e: Exception) {
                        // Ignore deletion errors
                    }
                }
            }
        }
        dao.deleteAll()
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private fun AlertEventEntity.toDomain() = AlertEvent(
        id         = vendorEventId,
        title      = edgeEventType.replace("_", " "),
        reason     = reason,
        severity   = AlertSeverity.fromString(severity),
        occurredAt = Instant.parse(occurredAt),
        imageUrl   = mediaRefs.firstOrNull() ?: "",
        mediaRefs  = mediaRefs,
        confidence = confidence,
        burstCount = burstCount,
        isFlagged  = isFlagged,
        isMock     = isMock
    )
}
