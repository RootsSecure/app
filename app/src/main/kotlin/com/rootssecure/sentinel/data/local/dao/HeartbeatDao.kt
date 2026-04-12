package com.rootssecure.sentinel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartbeatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(heartbeat: HeartbeatEntity)

    /** Observe the last 24 heartbeat records for the telemetry charts. */
    @Query("SELECT * FROM heartbeats ORDER BY recorded_at DESC LIMIT 24")
    fun observeLast24(): Flow<List<HeartbeatEntity>>

    /** Most recent heartbeat only — for dashboard status card. */
    @Query("SELECT * FROM heartbeats ORDER BY recorded_at DESC LIMIT 1")
    fun observeLatest(): Flow<HeartbeatEntity?>

    /** Remove records older than 25 hours to limit DB size. */
    @Query("DELETE FROM heartbeats WHERE recorded_at < :thresholdEpochMs")
    suspend fun deleteOlderThan(thresholdEpochMs: Long)
}
