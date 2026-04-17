package com.rootssecure.sentinel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON contract for the hourly hardware heartbeat published by the Pi.
 *
 * Example MQTT payload on topic "sentinel/nri-rpi-001/heartbeat":
 * {
 *   "cpu_temp_c": 45.2,
 *   "network_latency_ms": 42,
 *   "power_status": "direct_power",
 *   "ram_usage_percent": 15.5,
 *   "storage_usage_percent": 20.0,
 *   "battery_percent": 100
 * }
 */
@Serializable
data class HeartbeatDto(
    @SerialName("cpu_temp_c")          val cpuTempC: Double,
    @SerialName("network_latency_ms")  val networkLatencyMs: Int,
    @SerialName("power_status")        val powerStatus: String,
    @SerialName("ram_usage_percent")   val ramUsagePercent: Double = 0.0,
    @SerialName("storage_usage_percent") val storageUsagePercent: Double = 0.0,
    @SerialName("battery_percent")     val batteryPercent: Int = 100,
    @SerialName("node_id")             val nodeId: String? = null,
    @SerialName("uplink_status")       val uplinkStatus: String? = null,
    @SerialName("firmware_version")    val firmwareVersion: String? = null
)
