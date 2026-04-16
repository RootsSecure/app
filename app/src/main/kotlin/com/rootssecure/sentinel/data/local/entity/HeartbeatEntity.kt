package com.rootssecure.sentinel.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity — stores hourly hardware heartbeat snapshots.
 *
 * We keep the last 24 records (24 hours) for the telemetry charts.
 * Older records are deleted by [HeartbeatDao.deleteOlderThan].
 */
@Entity(tableName = "heartbeats")
data class HeartbeatEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")               val id: Long = 0,
    @ColumnInfo(name = "cpu_temp_c")       val cpuTempC: Double,
    @ColumnInfo(name = "network_latency_ms") val networkLatencyMs: Int,
    @ColumnInfo(name = "power_status")     val powerStatus: String,
    @ColumnInfo(name = "ram_usage_percent") val ramUsagePercent: Double,
    @ColumnInfo(name = "storage_usage_percent") val storageUsagePercent: Double,
    @ColumnInfo(name = "battery_percent") val batteryPercent: Int,
    @ColumnInfo(name = "is_mock")          val isMock: Boolean = false,
    @ColumnInfo(name = "recorded_at")      val recordedAt: Long   // epoch ms
)
