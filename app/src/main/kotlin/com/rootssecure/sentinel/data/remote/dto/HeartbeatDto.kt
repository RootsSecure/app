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
 *   "power_status": "direct_power"
 * }
 */
@Serializable
data class HeartbeatDto(
    @SerialName("cpu_temp_c")          val cpuTempC: Double,
    @SerialName("network_latency_ms")  val networkLatencyMs: Int,
    @SerialName("power_status")        val powerStatus: String  // "direct_power" | "battery_fallback"
)
