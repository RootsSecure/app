package com.rootssecure.nriplotsentinel.repository

import com.rootssecure.nriplotsentinel.api.ApiService
import com.rootssecure.nriplotsentinel.api.DashboardBundle
import com.rootssecure.nriplotsentinel.api.DeviceStatusResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SentinelRepository(
    private val apiService: ApiService
) {
    suspend fun fetchDashboardBundle(): DashboardBundle = coroutineScope {
        val dashboardDeferred = async { apiService.getDashboard() }
        val alertsDeferred = async { apiService.getAlerts() }
        val devicesDeferred = async { apiService.getDevices() }

        DashboardBundle(
            dashboard = dashboardDeferred.await(),
            alerts = alertsDeferred.await().alerts,
            devices = devicesDeferred.await().devices
        )
    }

    suspend fun fetchDeviceStatus(): DeviceStatusResponse {
        return apiService.getDeviceStatus()
    }
}
