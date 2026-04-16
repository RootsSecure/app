# RootsSecure (NRI Plot Sentinel) - Technical Specification

**Version**: 1.0.0  
**Last Updated**: 2026-04-15  
**Owner**: NRI Plot Sentinel Team  

## 1. Project Overview
RootsSecure is a privacy-first, local-first Command Center for the NRI Plot Sentinel Edge AI system. It provides property owners with real-time digital surveillance of their plots, focusing on detecting unauthorized construction or intrusion without relying on cloud-based processing.

### Core Value Proposition
- **Sovereign Data**: All video and image processing happens locally on the Edge Node (Raspberry Pi).
- **Zero Latency**: Direct Peer-to-Peer (P2P) communication via MQTT.
- **Offline Reliability**: Local caching of all alerts and history using an on-device database.

---

## 2. Technology Stack

### Android Command Center
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Reactive State-Flow)
- **Dependency Injection**: Hilt (Dagger)
- **Database**: Room (SQLite)
- **Networking**: HiveMQ MQTT Client (v3 Async)
- **Serialization**: Kotlin Serialization (JSON)
- **Image Loading**: Coil (likely, for alert frames)

### Edge Node (Client)
- **Language**: Python 3.10+
- **AI Framework**: OpenCV, PyTorch (Best.pt weights)
- **Inference**: Custom `RestorationPipeline` for "Adverse Vision" mitigation (Restoration of weather-degraded frames).
- **Communication**: Mosquitto MQTT Broker.

---

## 3. System Architecture

The project follows a strict **Clean Architecture** pattern to ensure modularity and testability.

### 🏢 UI Layer (`com.rootssecure.sentinel.ui`)
- **MVVM Pattern**: ViewModels derive `UiState` from domain flows via `stateIn(SharingStarted.WhileSubscribed)`.
- **Navigation**: `AppNavHost` manages the stack between Dashboard, Timeline, and Alert Details.
- **Theme**: Premium "Obsidian & Neon Teal" dark mode using curated HSL color tokens.

### 🏛️ Domain Layer (`com.rootssecure.sentinel.domain`)
- **Models**: Pure Kotlin data classes (`AlertEvent`, `Heartbeat`).
- **Use Cases**: Encapsulate business logic (e.g., `GetEdgeNodeStatusUseCase`).
- **Severity Logic**: 
    - `CRITICAL`: Intrusion/Illegal Construction.
    - `HIGH`: Motion detected in exclusion zones.
    - `INFO`: Periodic heartbeat and status.

### 💾 Data Layer (`com.rootssecure.sentinel.data`)
- **MQTT Service**: An Android Foreground Service that maintains the socket connection. Replaces Firebase Cloud Messaging (FCM) entirely.
- **Local Persistence**: Room acts as the "Single Source of Truth". ViewModels observe Room, while the MQTT service pushes updates to it.

---

## 4. Communication Protocol (MQTT)

The Android app communicates with the Raspberry Pi at `192.168.4.1:1883` (Local Hotspot).

### Topics
| Topic | Description |
| :--- | :--- |
| `sentinel/+/alerts` | High-priority security event JSON payloads. |
| `sentinel/+/heartbeat` | Hourly hardware metrics (CPU, Battery). |
| `sentinel/+/status_ping` | Periodic heartbeat for connection status. |

### Alert Payload Schema (`AlertEventDto`)
```json
{
  "alert_type": "manual_report | auto_detection",
  "vendor_event_id": "evt_TYPE_TIMESTAMP",
  "occurred_at": "ISO-8601 String",
  "metadata_json": {
    "edge_event_type": "ILLEGAL_CONSTRUCTION | INTRUSION",
    "recommended_severity": "CRITICAL",
    "reason": "AI Reason (e.g. JCB detected > 5 frames)",
    "logic_level": "CRITICAL",
    "motion_ratio": 0.85
  },
  "media_refs": ["https://.../processed_frame.jpg"]
}
```

---

## 5. Edge AI Logic: Adverse Vision Pipeline

The Edge Node employs a **Restoration Pipeline** to ensure that visual proof is clear even in sub-optimal conditions.

1.  **Frame Capture**: OpenCV captures 1080p stream from the camera.
2.  **Restoration**: The `RestorationPipeline` (using `best.pt` weights) mitigates noise and artifacts caused by weather or poor lighting.
3.  **Inference**: A secondary detection model (YOLO-based) identifies construction machinery (Tractors, JCBs, excavators).
4.  **Action**: If the detection persists for >5 frames, an `AlertEvent` is published to the MQTT broker.

---

## 6. Deployment & Configuration

### Prerequisites
- **Pi Hardware**: Raspberry Pi 4 (4GB+) + Camera Module.
- **Network**: The Pi must act as a Wi-Fi Access Point (AP) or be reachable over a local subnet.
- **Services**: A `nri-plot-sentinel-pi.service` systemd unit manages the client on the Pi.

### Dev Setup
1. Clone the repository.
2. Set up `local.properties` with appropriate SDK paths.
3. Run `./gradlew assembleDebug` to build the Android APK.
