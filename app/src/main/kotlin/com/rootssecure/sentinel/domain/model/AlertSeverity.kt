package com.rootssecure.sentinel.domain.model

/**
 * Sealed class representing the two alert severity levels defined by the
 * Edge AI rule engine on the Raspberry Pi.
 *
 * - [Critical] → Rule 1 (JCB >5 frames) or Rule 3 (Tractor stationary >5 min)
 * - [High]     → Rule 2 (Tractor + Person/Shovel in same frame)
 */
sealed class AlertSeverity {
    object Critical : AlertSeverity()
    object High     : AlertSeverity()

    companion object {
        fun fromString(value: String): AlertSeverity = when (value.uppercase()) {
            "CRITICAL" -> Critical
            "HIGH"     -> High
            else       -> High  // default to HIGH for unknown values
        }
    }

    override fun toString(): String = when (this) {
        is Critical -> "CRITICAL"
        is High     -> "HIGH"
    }
}
