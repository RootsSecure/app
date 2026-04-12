package com.rootssecure.sentinel.domain.model

import java.time.Instant

/** Domain model for a single Pi hardware heartbeat snapshot. */
data class Heartbeat(
    val cpuTempC: Double,
    val networkLatencyMs: Int,
    val powerStatus: PowerStatus,
    val recordedAt: Instant
)

sealed class PowerStatus {
    object DirectPower      : PowerStatus()
    object BatteryFallback  : PowerStatus()

    companion object {
        fun fromString(value: String): PowerStatus = when (value.lowercase()) {
            "direct_power"     -> DirectPower
            "battery_fallback" -> BatteryFallback
            else               -> DirectPower
        }
    }
}
