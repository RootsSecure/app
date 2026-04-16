package com.rootssecure.sentinel.data.mqtt

/**
 * Central configuration object for the MQTT broker connection.
 *
 * In production, [brokerHost] should be loaded from SharedPreferences /
 * DataStore (configured in the Provisioning screen) so the user can update
 * the Pi's IP address or domain without rebuilding the app.
 *
 * Defaults are the Raspberry Pi's local Wi-Fi AP address when you configure
 * it as a Wi-Fi hotspot (192.168.4.1) or the hotspot DHCP-assigned address.
 */
data class MqttConfig(
    val brokerHost: String  = "192.168.4.1",
    val brokerPort: Int     = 1883,
    val clientId: String    = "sentinel-android-01",
    val username: String?   = null,      // optional — enable auth on Mosquitto
    val password: String?   = null,
    val keepAliveSeconds: Int = 60,
    val cleanSession: Boolean = true,
    val connectTimeoutSeconds: Int = 10,
    val reconnectDelay: Long = 2_000L   // ms before reconnect attempt (base backoff)
)
