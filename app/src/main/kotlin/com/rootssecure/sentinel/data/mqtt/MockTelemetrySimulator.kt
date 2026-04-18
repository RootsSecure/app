package com.rootssecure.sentinel.data.mqtt

import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity
import kotlinx.coroutines.*
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockTelemetrySimulator @Inject constructor(
    private val heartbeatDao: HeartbeatDao,
    private val alertDao: AlertEventDao
) {
    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startSimulation() {
        if (simulationJob?.isActive == true) return
        
        simulationJob = scope.launch {
            launch { simulateHeartbeats() }
            launch { simulateAlerts() }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        
        // Wipe all mock data to prevent stale UI
        scope.launch {
            clearMockData()
        }
    }

    private suspend fun clearMockData() {
        heartbeatDao.deleteMockData()
        alertDao.deleteMockData()
    }

    private suspend fun simulateHeartbeats() {
        var baseTemp = 42.0
        while (currentCoroutineContext().isActive) {
            // Gradually fluctuate temp
            baseTemp += Random.nextDouble(-0.5, 0.5)
            if (baseTemp < 35.0) baseTemp = 36.0
            if (baseTemp > 75.0) baseTemp = 74.0

            val hb = HeartbeatEntity(
                cpuTempC            = baseTemp,
                networkLatencyMs    = Random.nextInt(20, 150),
                powerStatus         = if (Random.nextFloat() > 0.95) "battery_fallback" else "direct_power",
                ramUsagePercent     = Random.nextDouble(15.0, 60.0),
                storageUsagePercent = 24.5,
                batteryPercent      = 98,
                nodeId              = "MOCK_NODE_99",
                uplinkStatus        = "CLOUD_CONNECTED",
                firmwareVersion     = "2.0.0-mock",
                isMock              = true,
                recordedAt          = Instant.now().toEpochMilli()
            )
            heartbeatDao.insert(hb)
            delay(5_000) // 5 seconds
        }
    }

    private suspend fun simulateAlerts() {
        val alertTypes = listOf(
            "CONSTRUCTION_VEHICLE_DETECTED" to "Heavy machinery operating in restricted zone",
            "LABOURS_DETECTED"             to "Workforce detected at unauthorized hour"
        )

        while (currentCoroutineContext().isActive) {
            delay(Random.nextLong(30_000, 60_000)) // 30-60 seconds
            
            val (type, reason) = alertTypes.random()
            val burstCount = Random.nextInt(2, 5)
            val mediaRefs = List(burstCount) { i -> "https://picsum.photos/seed/${Random.nextInt()}/1080/720" }
            
            val alert = AlertEventEntity(
                vendorEventId  = UUID.randomUUID().toString(),
                alertType      = type,
                occurredAt     = Instant.now().toString(),
                edgeEventType  = type,
                severity       = if (type == "CONSTRUCTION_VEHICLE_DETECTED" || type == "SECURITY_BREACH") "CRITICAL" else "WARNING",
                reason         = reason,
                logicLevel     = null,
                motionRatio    = Random.nextDouble(0.1, 0.4),
                burstCount     = burstCount,
                confidence     = Random.nextDouble(0.7, 0.99),
                mediaRefs      = mediaRefs,
                nodeId         = "MOCK_NODE_99",
                receivedAt     = Instant.now().toEpochMilli(),
                isFlagged      = false,
                isMock         = true
            )
            alertDao.insert(alert)
        }
    }
}
