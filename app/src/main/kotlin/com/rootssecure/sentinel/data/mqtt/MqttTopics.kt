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

    fun alerts(nodeId: String)      = "$ROOT/$nodeId/alerts"
    fun heartbeat(nodeId: String)   = "$ROOT/$nodeId/heartbeat"
    fun status(nodeId: String)      = "$ROOT/$nodeId/status"
    fun snapshot(nodeId: String)    = "$ROOT/$nodeId/snapshot"
    fun allNode(nodeId: String)     = "$ROOT/$nodeId/#"

    /** QoS levels */
    const val QOS_AT_MOST_ONCE  = 0   // heartbeat pings
    const val QOS_AT_LEAST_ONCE = 1   // alerts, snapshots (must not be lost)
    const val QOS_EXACTLY_ONCE  = 2   // provisioning confirmations
}
