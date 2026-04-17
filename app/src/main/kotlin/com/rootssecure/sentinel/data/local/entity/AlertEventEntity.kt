package com.rootssecure.sentinel.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity — persists every MQTT alert received from the Pi.
 * [vendorEventId] is used as the primary key to prevent duplicate inserts
 * if the MQTT broker re-delivers a message (QoS 1 "at least once").
 */
@Entity(
    tableName = "alert_events",
    indices   = [Index(value = ["occurred_at"]), Index(value = ["severity"])]
)
data class AlertEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "vendor_event_id") val vendorEventId: String,
    @ColumnInfo(name = "alert_type")      val alertType: String,
    @ColumnInfo(name = "occurred_at")     val occurredAt: String,
    @ColumnInfo(name = "edge_event_type") val edgeEventType: String,
    @ColumnInfo(name = "severity")        val severity: String,
    @ColumnInfo(name = "reason")          val reason: String,
    @ColumnInfo(name = "logic_level")     val logicLevel: String,
    @ColumnInfo(name = "motion_ratio")    val motionRatio: Double,
    @ColumnInfo(name = "media_ref")       val mediaRef: String,
    @ColumnInfo(name = "node_id")         val nodeId: String? = null,
    @ColumnInfo(name = "received_at")     val receivedAt: Long,   // epoch ms
    @ColumnInfo(name = "is_flagged")      val isFlagged: Boolean = false,
    @ColumnInfo(name = "is_mock")         val isMock: Boolean = false
)
