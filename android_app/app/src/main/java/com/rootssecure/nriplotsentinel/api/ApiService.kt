package com.rootssecure.nriplotsentinel.api

import retrofit2.http.GET

interface ApiService {
    @GET("api/v1/mobile/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("api/v1/mobile/alerts")
    suspend fun getAlerts(): AlertsResponse

    @GET("api/v1/mobile/devices")
    suspend fun getDevices(): DevicesResponse

    @GET("api/v1/mobile/device/status")
    suspend fun getDeviceStatus(): DeviceStatusResponse

    @GET("api/v1/mobile/events")
    suspend fun getEvents(): EventsResponse
}
