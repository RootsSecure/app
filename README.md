# RootsSecure Native (NRI Plot Sentinel)

RootsSecure is a high-performance, native Android command center for the NRI Plot Sentinel Edge AI system. It implements a "local-first" architecture, communicating directly with Raspberry Pi Edge Nodes via MQTT to ensure maximum privacy, speed, and offline capability.

## 🚀 Key Features
- **Local-First Engineering**: No reliance on centralized cloud servers. Direct RPi-to-Mobile communication.
- **Digital Panopticon UI**: Premium "Obsidian & Neon Teal" glassmorphism dashboard built with Jetpack Compose.
- **Real-Time Alert Engine**: Receives security events (JCB detection, illegal construction, encroachment) in <100ms via MQTT.
- **Telemetry Visualizer**: Live hardware monitoring (CPU Temp, 4G Latency, Power Status).
- **Foreground Alerting**: Persistent background service ensures you never miss a critical alert, even when the app is closed.
- **BLE Provisioning**: Easy setup for new Edge Nodes over Bluetooth LE.

## 🛠 Tech Stack
- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (MVVM + Domain Layer)
- **Dependency Injection**: Hilt
- **Local Database**: Room (with Flow-based reactive updates)
- **Networking**: HiveMQ MQTT Client & Kotlin Serialization
- **Image Loading**: Coil 3
- **Async**: Coroutines & Flow

## 📂 Project Structure
```text
app/src/main/kotlin/com/rootssecure/sentinel/
├── data/           # Repositories, DAOs, Entities, MQTT Service, DTOs
├── domain/         # Pure Kotlin models, Repository interfaces, UseCases
├── di/             # Hilt Dependency Injection modules
└── ui/             # Compose Screens, ViewModels, Themes, Navigation
```

## ⚙️ Development Setup
1. Open the project in **Android Studio Iguana** or newer.
2. Ensure **JDK 17** is configured.
3. Sync Gradle and build the APK.
4. (Optional) Configure your Pi's MQTT broker IP in `MqttConfig.kt` or via the in-app Provisioning screen.

## 📡 MQTT Contract
The app listens on the following topics:
- `sentinel/+/alerts`: Security event payloads (JSON).
- `sentinel/+/heartbeat`: Hourly hardware health snapshots.

---
© 2026 RootsSecure. All Rights Reserved.
