package com.rootssecure.sentinel.data.mqtt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.rootssecure.sentinel.MainActivity
import com.rootssecure.sentinel.R
import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity
import com.rootssecure.sentinel.data.remote.dto.AlertEventDto
import com.rootssecure.sentinel.data.remote.dto.HeartbeatDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.inject.Inject

/**
 * [MqttService] is the heart of the Firebase-free real-time architecture.
 *
 * It runs as an Android Foreground Service so it stays alive even when the
 * app is in the background. When the Raspberry Pi publishes an alert to the
 * Mosquitto broker, this service:
 *   1. Receives the raw JSON payload.
 *   2. Deserialises it into a DTO ([AlertEventDto] / [HeartbeatDto]).
 *   3. Persists it into Room DB via the appropriate DAO.
 *   4. Posts a high-priority Android notification (replaces FCM entirely).
 *   5. The app's ViewModels observe Room via `Flow`, so the UI updates
 *      automatically — zero polling required.
 *
 * Connection lifecycle:
 *   - Starts on [MainActivity] launch.
 *   - Reconnects automatically after network drops (exponential back-off).
 *   - Stops cleanly when [BootReceiver] hasn't restarted it (explicit user stop).
 */
@AndroidEntryPoint
class MqttService : Service() {

    @Inject lateinit var alertDao: AlertEventDao
    @Inject lateinit var heartbeatDao: HeartbeatDao
    @Inject lateinit var propertyDao: com.rootssecure.sentinel.data.local.entity.PropertyDao
    @Inject lateinit var configRepo: com.rootssecure.sentinel.domain.repository.MqttConfigRepository

    private var currentMqttTopicId: String? = null
    private var currentConfig: MqttConfig? = null

    private val serviceJob  = SupervisorJob()
    private val scope       = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var mqttClient: Mqtt3AsyncClient

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        // Observe config changes for live reconnection
        scope.launch {
            configRepo.config.collect { config ->
                if (currentConfig != config) {
                    currentConfig = config
                    Log.d(TAG, "MQTT Config changed — re-initializing client")
                    MqttLogManager.log("Network settings updated. Reconnecting...")
                    
                    // Terminate existing connection
                    if (::mqttClient.isInitialized) {
                        try {
                            mqttClient.disconnect().get()
                        } catch (e: Exception) {
                            Log.w(TAG, "Disconnect failed during reconfig", e)
                        }
                    }
                    
                    buildMqttClient()
                    connectAndSubscribe()
                }
            }
        }

        // Observe active property to handle dynamic site switching
        scope.launch {
            propertyDao.getActiveProperty().collect { property ->
                val newTopicId = property?.mqttTopicId
                if (newTopicId != currentMqttTopicId) {
                    currentMqttTopicId = newTopicId
                    Log.d(TAG, "Active site changed to $newTopicId — re-subscribing")
                    if (::mqttClient.isInitialized && mqttClient.state.isConnected) {
                        resubscribe()
                    }
                }
            }
        }
    }

    private fun resubscribe() {
        if (currentMqttTopicId == null) return
        
        MqttLogManager.log("Re-subscribing for Node: $currentMqttTopicId")
        // In MQTT v3 Async Client, we just send new subscriptions
        subscribeToAlerts()
        subscribeToHeartbeat()
        subscribeToStatusPing()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        connectAndSubscribe()
        return START_STICKY   // system restarts service if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mqttClient.disconnect()
        serviceJob.cancel()
        super.onDestroy()
    }

    // ── MQTT Client ──────────────────────────────────────────────────────────

    private fun buildMqttClient() {
        val config = currentConfig ?: return
        val builder = MqttClient.builder()
            .useMqttVersion3()
            .identifier(config.clientId)
            .serverHost(config.brokerHost)
            .serverPort(config.brokerPort)

        if (config.useTls) {
            builder.sslWithDefaultConfig()
        }

        mqttClient = builder.buildAsync()
    }

    private fun connectAndSubscribe() {
        scope.launch {
            val backoffSequence = listOf(2000L, 4000L, 8000L, 16000L)
            var backoffIndex = 0
            
            while (true) {
                try {
                    if (!::mqttClient.isInitialized) {
                        Log.d(TAG, "MqttClient not yet initialized, waiting...")
                        delay(1000)
                        continue
                    }

                    val state = mqttClient.state
                    if (state.isConnected) {
                        Log.d(TAG, "Already connected, skipping connection attempt")
                        break
                    }

                    val config = currentConfig ?: break
                    MqttLogManager.log("Attempting connection to ${config.brokerHost}...")
                    
                    val connectBuilder = mqttClient.connectWith()
                        .cleanSession(config.cleanSession)
                        .keepAlive(config.keepAliveSeconds)

                    if (config.username != null && config.password != null) {
                        connectBuilder.simpleAuth()
                            .username(config.username)
                            .password(config.password.toByteArray())
                            .applySimpleAuth()
                    }

                    connectBuilder.send().get()   // block coroutine until connected

                    MqttConnectionManager.setConnected(true)
                    MqttLogManager.log("Connected to ${config.brokerHost}:${config.brokerPort}")
                    Log.i(TAG, "MQTT connected → ${config.brokerHost}:${config.brokerPort}")
                    backoffIndex = 0 // reset on success

                    subscribeToAlerts()
                    subscribeToHeartbeat()
                    subscribeToStatusPing()
                    break   // exit retry loop — subscriptions are now live

                } catch (e: Exception) {
                    MqttConnectionManager.setConnected(false)
                    MqttConnectionManager.setError(e.localizedMessage)
                    val currentDelay = backoffSequence[backoffIndex]
                    MqttLogManager.log("Connection failed: ${e.message}. Retrying in ${currentDelay/1000}s")
                    Log.w(TAG, "MQTT connect failed — retrying in ${currentDelay}ms", e)
                    
                    delay(currentDelay)
                    
                    // Move to next backoff step, cap at 16s
                    if (backoffIndex < backoffSequence.lastIndex) {
                        backoffIndex++
                    }
                }
            }
        }
    }

    private fun subscribeToAlerts() {
        val topic = MqttTopics.alerts(currentMqttTopicId ?: "+")
        MqttLogManager.log("Subscribing to $topic")
        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
            .callback { publish -> handleAlert(publish) }
            .send()
    }

    private fun subscribeToHeartbeat() {
        val topic = MqttTopics.heartbeat(currentMqttTopicId ?: "+")
        MqttLogManager.log("Subscribing to $topic")
        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_MOST_ONCE)
            .callback { publish -> handleHeartbeat(publish) }
            .send()
    }

    private fun subscribeToStatusPing() {
        val topic = MqttTopics.status(currentMqttTopicId ?: "+")
        MqttLogManager.log("Subscribing to $topic")
        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_MOST_ONCE)
            .callback { _ -> 
                Log.d(TAG, "Pi status ping received")
                MqttLogManager.log("Status ping received from node")
            }
            .send()
    }

    // ── Message Handlers ─────────────────────────────────────────────────────

    private fun handleAlert(publish: Mqtt3Publish) {
        val payload = publish.payloadAsBytes.toString(StandardCharsets.UTF_8)
        Log.d(TAG, "Alert received: $payload")
        MqttLogManager.log("ALERT INCOMING: $payload")
        scope.launch {
            try {
                val dto    = json.decodeFromString<AlertEventDto>(payload)
                val entity = dto.toEntity()
                alertDao.insert(entity)

                val severity = dto.metadataJson.logicLevel
                postAlertNotification(
                    title   = dto.metadataJson.edgeEventType.replace("_", " "),
                    message = dto.metadataJson.reason,
                    isCritical = severity.equals("CRITICAL", ignoreCase = true)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse alert payload", e)
                MqttLogManager.log("ERROR: Failed to parse alert JSON")
            }
        }
    }

    private fun handleHeartbeat(publish: Mqtt3Publish) {
        val payload = publish.payloadAsBytes.toString(StandardCharsets.UTF_8)
        Log.d(TAG, "Heartbeat received: $payload")
        MqttLogManager.log("HEARTBEAT RECEIVED: $payload")
        scope.launch {
            try {
                val dto    = json.decodeFromString<HeartbeatDto>(payload)
                val entity = dto.toEntity()
                heartbeatDao.insert(entity)

                // Warn if CPU temperature exceeds safety threshold
                if (dto.cpuTempC > 80.0) {
                    postHeartbeatWarning("CPU Temperature Critical: ${dto.cpuTempC}°C")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse heartbeat payload", e)
                MqttLogManager.log("ERROR: Failed to parse heartbeat JSON")
            }
        }
    }

    // ── Notifications ────────────────────────────────────────────────────────

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        // Foreground service channel (silent)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVICE, "Sentinel Active",
                NotificationManager.IMPORTANCE_MIN
            ).apply { description = "Background MQTT connection status" }
        )

        // Alert channel (high importance — heads-up notification)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS, "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Intrusion and threat alerts from your plot"
                enableVibration(true)
                enableLights(true)
                lightColor = 0xFFEF4444.toInt()
            }
        )

        // Heartbeat warning channel
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_HEARTBEAT, "Hardware Warnings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Pi hardware health warnings" }
        )
    }

    private fun buildForegroundNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setContentTitle("Plot Sentinel Active")
            .setContentText("Monitoring ${currentConfig?.brokerHost ?: "Broker"} via MQTT")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    private fun postAlertNotification(title: String, message: String, isCritical: Boolean) {
        val intent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(),
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ALERTS)
            .setContentTitle("⚠ $title")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .apply {
                if (isCritical) {
                    setFullScreenIntent(intent, true)   // wake screen for CRITICAL
                    setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                }
            }
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun postHeartbeatWarning(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_HEARTBEAT)
            .setContentTitle("Hardware Warning")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    // ── Extension Mappers ────────────────────────────────────────────────────

    private fun processMediaRefs(refs: List<String>): List<String> {
        return refs.map { ref ->
            // If it's a standard URL or already a local file URI, pass it through.
            if (ref.startsWith("http://") || ref.startsWith("https://") || ref.startsWith("file://")) {
                ref
            } else {
                // Otherwise, treat it as a raw Base64 encoded image payload.
                try {
                    // Remove potential data URI prefix (e.g. "data:image/jpeg;base64,")
                    val base64String = if (ref.contains(",")) ref.substringAfter(",") else ref
                    val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    
                    // Save to local app cache
                    val file = java.io.File(cacheDir, "evidence_${java.util.UUID.randomUUID()}.jpg")
                    java.io.FileOutputStream(file).use { it.write(decodedBytes) }
                    
                    // Return the local file URI to be stored in the database
                    "file://${file.absolutePath}"
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to decode inline base64 image", e)
                    ref // Fallback (might crash DB if too large, but prevents immediate loss)
                }
            }
        }
    }

    private fun AlertEventDto.toEntity() = AlertEventEntity(
        vendorEventId = vendorEventId,
        alertType     = alertType,
        occurredAt    = occurredAt,
        edgeEventType = metadataJson.edgeEventType,
        severity      = metadataJson.recommendedSeverity,
        reason        = metadataJson.reason,
        logicLevel    = metadataJson.logicLevel,
        motionRatio   = metadataJson.motionRatio,
        burstCount    = metadataJson.burstCount,
        confidence    = metadataJson.confidence,
        mediaRefs     = processMediaRefs(mediaRefs),
        nodeId        = nodeId ?: currentMqttTopicId,
        receivedAt    = Instant.now().toEpochMilli(),
        isMock        = false
    )

    private fun HeartbeatDto.toEntity() = HeartbeatEntity(
        cpuTempC          = cpuTempC,
        networkLatencyMs  = networkLatencyMs,
        powerStatus       = powerStatus,
        ramUsagePercent    = ramUsagePercent,
        storageUsagePercent = storageUsagePercent,
        batteryPercent     = batteryPercent,
        nodeId             = nodeId ?: currentMqttTopicId,
        uplinkStatus       = uplinkStatus,
        firmwareVersion    = firmwareVersion,
        isMock            = false,
        recordedAt        = Instant.now().toEpochMilli()
    )

    companion object {
        private const val TAG                = "MqttService"
        private const val NOTIFICATION_ID    = 1001
        private const val CHANNEL_SERVICE    = "sentinel_service"
        private const val CHANNEL_ALERTS     = "sentinel_alerts"
        private const val CHANNEL_HEARTBEAT  = "sentinel_heartbeat"
        private const val MAX_RECONNECT_DELAY = 60_000L   // 60 seconds max
    }
}
