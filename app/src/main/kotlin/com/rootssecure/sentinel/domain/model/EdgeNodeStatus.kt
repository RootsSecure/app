package com.rootssecure.sentinel.domain.model

/** Represents the real-time connection state between the app and the Pi MQTT broker. */
sealed class EdgeNodeStatus {
    /** MQTT connected and last status ping received within threshold. */
    data class Online(val lastSeenMs: Long) : EdgeNodeStatus()

    /** MQTT connected but no status ping received recently. */
    object Stale : EdgeNodeStatus()

    /** MQTT client is not connected (network issue / Pi offline). */
    object Offline : EdgeNodeStatus()

    /** Still waiting for first connection attempt to resolve. */
    object Connecting : EdgeNodeStatus()
}
