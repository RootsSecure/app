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
    val brokerHost: String  = "eef332c6869d4dfe823751b6c18ebbe6.s1.eu.hivemq.cloud",
    val brokerPort: Int     = 8883,
    val useTls: Boolean     = true,
    val clientId: String    = "sentinel-mobile-client",
    val username: String?   = "gunjan",
    val password: String?   = "Gunjan123@",
    val keepAliveSeconds: Int = 60,
    val cleanSession: Boolean = true,
    val connectTimeoutSeconds: Int = 20,
    val reconnectDelay: Long = 2_000L
)
