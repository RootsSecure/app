package com.rootssecure.sentinel.domain.repository

import com.rootssecure.sentinel.domain.model.AlertEvent
import kotlinx.coroutines.flow.Flow

/** Repository interface — no Android / Room types. Implemented in data layer. */
interface AlertRepository {
    /** Reactive stream of all alerts ordered newest-first. Updates on new MQTT messages. */
    fun observeAll(): Flow<List<AlertEvent>>

    /** Reactive stream of CRITICAL-only alerts. */
    fun observeCritical(): Flow<List<AlertEvent>>

    /** Get a single alert by its vendor event ID. Returns null if not found. */
    suspend fun getById(id: String): AlertEvent?

    /** Mark an alert as a false alarm (does not delete it). */
    suspend fun flagAsFalseAlarm(id: String)
}
