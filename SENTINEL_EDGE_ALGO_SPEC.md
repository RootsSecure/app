# SENTINEL: Edge Node (Raspberry Pi) Integration Specification

**Version:** 1.0.0  
**Target Hardware:** Raspberry Pi 4B (4GB+ RAM recommended)  
**Connectivity:** Local Wi-Fi Access Point (DHCP Gateway)

---

## 1. Connectivity Requirements

The Raspberry Pi must be configured as a standalone Wireless Access Point (AP) to allow the mobile app to connect directly without an internet dependency.

- **SSID:** `SENTINEL_NODE_<ID>`
- **Gateway IP:** `192.168.4.1`
- **MQTT Broker:** Mosquitto
  - **Port:** `1883`
  - **Config:**
    ```conf
    listener 1883 0.0.0.0
    allow_anonymous true
    ```

---

## 2. Payload Contract (JSON)

The Edge Node must publish payloads to the following topic hierarchy: `sentinel/<node_id>/<category>`.

### A. Security Alerts
**Topic:** `sentinel/+/alerts`

```json
{
  "vendor_event_id": "uuid-v4-string",
  "alert_type": "Auto", 
  "occurred_at": "2026-04-17T04:25:38Z",
  "metadata_json": {
    "edge_event_type": "PERSON_DETECTED",
    "recommended_severity": "HIGH",
    "logic_level": "CRITICAL",
    "reason": "Unknown subject detected in restricted zone",
    "motion_ratio": 0.85
  },
  "media_refs": [
    "http://192.168.4.1/media/alerts/event_123.jpg"
  ]
}
```

### B. Hardware Heartbeat (Every 60s)
**Topic:** `sentinel/+/heartbeat`

```json
{
  "cpu_temp_c": 42.5,
  "ram_usage_percent": 24.1,
  "battery_percent": 85,
  "network_latency_ms": 12,
  "power_status": "AC_CONNECTED",
  "storage_usage_percent": 15.4
}
```

---

## 3. Image Handling (Adverse Vision Pipeline)

The `media_refs` in the alert payload must be reachable by the mobile app.

1. **Storage:** Processed frames should be saved to `/var/www/html/media/alerts/`.
2. **Serving:** Run a lightweight web server (e.g., Nginx or `python3 -m http.server 80`) on the Pi.
3. **Reference:** Use absolute URLs pointing to the Pi's gateway IP (`http://192.168.4.1/...`).

---

## 4. Operational Requirements

- **Latency:** Heartbeats must be sent every 60 seconds. The mobile app will trigger a "No Device Connected" UI if no heartbeat is received for 120 seconds.
- **Reliability:** Use MQTT QoS 1 for Alerts and QoS 0 for Heartbeats.
- **Startup:** The Python detection script and Mosquitto broker must start automatically on boot (via `systemd`).
