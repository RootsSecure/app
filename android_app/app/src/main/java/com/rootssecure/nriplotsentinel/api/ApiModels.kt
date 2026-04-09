package com.rootssecure.nriplotsentinel.api

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("property_count") val propertyCount: Int = 0,
    @SerializedName("active_alerts") val activeAlerts: Int = 0,
    @SerializedName("online_devices") val onlineDevices: Int = 0,
    @SerializedName("protected_perimeter") val protectedPerimeter: String = "0%",
    @SerializedName("owner_name") val ownerName: String = "NRI Owner"
)

data class AlertsResponse(
    @SerializedName("alerts") val alerts: List<AlertItem> = emptyList()
)

data class DevicesResponse(
    @SerializedName("devices") val devices: List<DeviceItem> = emptyList()
)

data class EventsResponse(
    @SerializedName("events") val events: List<EventHistoryItem> = emptyList()
)

data class AlertItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("location") val location: String,
    @SerializedName("occurred_at") val occurredAt: String,
    @SerializedName("status") val status: String = "open"
)

data class DeviceItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("last_seen") val lastSeen: String = ""
)

data class DeviceStatusResponse(
    @SerializedName("device_id") val deviceId: String = "",
    @SerializedName("device_name") val deviceName: String = "Raspberry Pi Gateway",
    @SerializedName("status") val status: String = "offline",
    @SerializedName("last_heartbeat_time") val lastHeartbeatTime: String = "Unknown",
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("network_status") val networkStatus: String = "unknown"
)

data class EventHistoryItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("alert_type") val alertType: String = "unknown",
    @SerializedName("occurred_at") val occurredAt: String = "",
    @SerializedName("media_refs") val mediaRefs: List<String> = emptyList()
)

data class DashboardBundle(
    val dashboard: DashboardResponse,
    val alerts: List<AlertItem>,
    val devices: List<DeviceItem>
)
