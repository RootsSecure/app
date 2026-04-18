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
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade

@HiltAndroidApp
class SentinelApp : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }
}
