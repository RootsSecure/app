package com.rootssecure.sentinel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [AlertEventEntity].
 *
 * [observeAll] returns a [Flow] — Room automatically emits a new list every
 * time the table changes, so the ViewModel doesn't need to poll.
 */
@Dao
interface AlertEventDao {

    /** Insert or ignore (idempotent — vendorEventId is the PK). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: AlertEventEntity)

    /** Observe all alerts ordered by occurrence time descending (newest first). */
    @Query("SELECT * FROM alert_events WHERE (:includeMock OR is_mock = 0) ORDER BY occurred_at DESC")
    fun observeAll(includeMock: Boolean): Flow<List<AlertEventEntity>>

    /** Observe only CRITICAL alerts. */
    @Query("SELECT * FROM alert_events WHERE severity = 'CRITICAL' AND (:includeMock OR is_mock = 0) ORDER BY occurred_at DESC")
    fun observeCritical(includeMock: Boolean): Flow<List<AlertEventEntity>>

    /** Get a single alert by its vendor ID (for the detail screen). */
    @Query("SELECT * FROM alert_events WHERE vendor_event_id = :id LIMIT 1")
    suspend fun getById(id: String): AlertEventEntity?

    /** Mark an alert as a false alarm. */
    @Query("UPDATE alert_events SET is_flagged = 1 WHERE vendor_event_id = :id")
    suspend fun flagAsFalseAlarm(id: String)

    /** Delete alerts older than [thresholdEpochMs] to limit storage usage. */
    @Query("DELETE FROM alert_events WHERE received_at < :thresholdEpochMs")
    suspend fun deleteOlderThan(thresholdEpochMs: Long)

    @Query("DELETE FROM alert_events WHERE is_mock = 1")
    suspend fun deleteMockData()

    @Query("SELECT * FROM alert_events")
    suspend fun getAllAlerts(): List<AlertEventEntity>

    @Query("DELETE FROM alert_events")
    suspend fun deleteAll()
}
