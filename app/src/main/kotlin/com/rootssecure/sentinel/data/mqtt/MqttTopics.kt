package com.rootssecure.sentinel.data.mqtt

/**
 * MQTT topic hierarchy published by the Raspberry Pi Edge Node.
 *
 * Convention: sentinel/<node_id>/<category>
 *
 * The Pi publishes to these topics; the Android app subscribes to all of
 * them using a wildcard: "sentinel/+/alerts", "sentinel/+/heartbeat", etc.
 *
 * For single-plot setups, you can subscribe to the literal strings below.
 * For multi-plot support, swap [NODE_ID] with "+" to receive from all nodes.
 */
object MqttTopics {
    private const val ROOT     = "sentinel"
    const val NODE_ID          = "nri-rpi-001"   // Update in provisioning

    /** Full-schema alert JSON payload (Rule 1 / 2 / 3 trigger events) */
    const val ALERTS           = "$ROOT/+/alerts"

    /** Hourly hardware heartbeat: cpu_temp_c, network_latency_ms, power_status */
    const val HEARTBEAT        = "$ROOT/+/heartbeat"

    /** Lightweight "I'm alive" ping every 60 s — used for online/offline status */
    const val STATUS_PING      = "$ROOT/+/status"

    /** Camera snapshot availability notification (payload = image URL) */
    const val SNAPSHOT_READY   = "$ROOT/+/snapshot"

    /** Wildcard to subscribe to ALL topics from this node */
    const val ALL_NODE_TOPICS  = "$ROOT/+/#"

    /** QoS levels */
    const val QOS_AT_MOST_ONCE  = 0   // heartbeat pings
    const val QOS_AT_LEAST_ONCE = 1   // alerts, snapshots (must not be lost)
    const val QOS_EXACTLY_ONCE  = 2   // provisioning confirmations
}
