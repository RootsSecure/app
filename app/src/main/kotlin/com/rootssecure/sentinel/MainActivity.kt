package com.rootssecure.sentinel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rootssecure.sentinel.data.mqtt.MqttService
import com.rootssecure.sentinel.ui.navigation.AppNavHost
import com.rootssecure.sentinel.ui.theme.SentinelTheme
import androidx.lifecycle.lifecycleScope
import com.rootssecure.sentinel.data.mqtt.MockTelemetrySimulator
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Compose navigation graph.
 *
 * Responsibilities:
 * - Configures edge-to-edge display so Compose controls the system bar appearance.
 * - Starts [MqttService] as a foreground service on first launch, keeping the
 *   MQTT connection alive even if the user navigates away.
 * - Hands off all UI rendering to [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var devSettings: DeveloperSettingsRepository
    @Inject lateinit var simulator: MockTelemetrySimulator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startMqttService()
        
        // Handle Developer Mode simulation
        lifecycleScope.launch {
            devSettings.isDeveloperModeEnabled
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) simulator.startSimulation()
                    else simulator.stopSimulation()
                }
        }

        setContent {
            SentinelTheme {
                AppNavHost()
            }
        }
    }

    private fun startMqttService() {
        val intent = Intent(this, MqttService::class.java)
        startForegroundService(intent)
    }
}
