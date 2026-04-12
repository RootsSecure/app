package com.rootssecure.sentinel

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Annotated with [HiltAndroidApp] to generate the
 * Hilt component hierarchy used across the entire app.
 *
 * No Firebase initialisation here — real-time alerts arrive via the MQTT
 * foreground service ([data.mqtt.MqttService]) which connects directly to
 * the Mosquitto broker running on the Raspberry Pi 4.
 */
@HiltAndroidApp
class SentinelApp : Application()
