# RootsSecure (NRI Plot Sentinel)

Welcome to **RootsSecure**! This is a complete, native Android command center for the NRI Plot Sentinel Edge AI system. It is designed to act as a digital watchtower for your property, communicating locally and securely.

## 🚀 What Does It Do?
RootsSecure monitors your plots by connecting directly to your smart camera nodes (Raspberry Pi). Without sending your private data to Google, AWS, or any other cloud provider, it delivers lightning-fast, highly secure notifications if anything suspicous occurs — like an illegal tractor moving dirt on your property.

### Key Beginner-Friendly Features
- **Local-First Privacy**: Internet connection is not required to receive alerts if you are on the same Wi-Fi network as the Pi. No data leaves your property.
- **Digital Panopticon Dashboard**: A premium, easy-to-read "Obsidian & Neon Teal" dark mode that highlights vital statistics using clear gauges.
- **Real-Time Visual Proof**: If an incident occurs, you immediately receive a 1080p picture right on your phone, complete with AI reasoning (e.g., "JCB actively detected for >5 minutes").
- **Never Miss an Alert**: A persistent background service automatically fetches alerts even when the screen is off or the app is closed.

---

## 🏗️ System Architecture

Our solution is divided into two major local hardware pieces that talk to each other without a middleman:

1. **The Edge Node (Raspberry Pi + Camera)** - Runs computer vision AI locally. It processes video feeds to detect construction machines and unauthorized personnel.
2. **The Command Center (RootsSecure Android App)** - This app. It subscribes to the Pi via MQTT. 

### The Under-the-Hood App Architecture
For developers contributing to this Android Application, we strictly use the **Clean Architecture** pattern to ensure it remains scalable and easy to maintain:

*   **UI Layer (Jetpack Compose)**: All buttons, gauges, and layouts are drawn natively. It uses a reactive State-Flow model (MVVM), meaning the screen always accurately reflects the latest data without manual refreshes.
*   **Domain Layer**: Pure business logic (e.g., what counts as a "Critical" vs "High" alert, and connection statuses). Located in `app/src/main/kotlin/.../domain`.
*   **Data Layer**: 
    *   **MQTT**: Uses HiveMQ to hold a constant local socket connection to the Raspberry Pi. Receives new alerts and heartbeat JSON payloads.
    *   **Room Database**: Instantly caches alerts inside the phone's local storage so you can review evidence logs even when completely offline. 

---

## ⚙️ How to Run the App (For Beginners)

1. **Download Android Studio**: It's a free application for making Android Apps. You can get it from [developer.android.com/studio](https://developer.android.com/studio).
2. **Open the Project**: Open Android Studio, click "Open", and select this folder (`D:\NRI app`).
3. **Connect Your Phone**: Plug your Android phone into your computer via a USB cable. (You'll need to enable "USB Debugging" in your phone's Developer Options).
4. **Hit the Play Button**: Click the green "Run" arrow at the top of Android Studio. It will automatically build the app and install it onto your phone!

## 📡 MQTT Technical Details (For Geeks)
By default, the app looks for an MQTT Broker hosted on the Raspberry Pi's local Wi-Fi Hotspot at `192.168.4.1:1883`. 

**Topics Subscribed:**
- `sentinel/+/alerts`: Listens for new security event JSONs.
- `sentinel/+/heartbeat`: Listens for hourly hardware health snaps (CPU temps, battery levels).

---
© 2026 RootsSecure. All Rights Reserved.
