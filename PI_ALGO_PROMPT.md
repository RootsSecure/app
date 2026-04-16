# Raspberry Pi Edge AI Algorithm Integration

To integrate your Raspberry Pi's detection capabilities with the RootsSecure Android app, configure your Edge AI logic (YOLOv8, TensorFlow Lite, or similar) to publish MQTT messages when specific classes are detected.

## 1. Class Mapping (Object Detection)
Configure your model to prioritize and map detected objects to these specific `edge_event_type` strings:

| Target Object | Event Type String | Recommended Severity |
| :--- | :--- | :--- |
| **Truck, Excavator, Crane** | `CONSTRUCTION_VEHICLE_DETECTED` | `CRITICAL` |
| **Human with Helmet/Vest** | `LABOURS_DETECTED` | `WARNING` |
| **Human (No specific gear)** | `SECURITY_BREACH` | `CRITICAL` |

## 2. MQTT Payload Structure
Publish a JSON payload to the `sentinel/alerts` topic in the following format:

```json
{
  "vendor_event_id": "uuid-v4-string",
  "edge_event_type": "CONSTRUCTION_VEHICLE_DETECTED",
  "logic_level": "CRITICAL",
  "reason": "Excavator operating in Sector B restricted zone",
  "media_ref": "http://<pi-ip>/media/snap_001.jpg",
  "occurred_at": "2024-04-17T10:00:00Z"
}
```

## 3. Implementation Prompt for Pi Dev
*Copy and use this prompt if you are using an LLM to generate the Pi's Python script:*

> "Write a Python script for Raspberry Pi that uses OpenCV and YOLOV8 to detect 'truck', 'excavator', and 'person' classes. 
> 1. When a 'person' is detected, check if they are wearing a 'helmet' or 'safety_vest'. If yes, label the event as 'LABOURS_DETECTED' with 'WARNING' severity.
> 2. When 'truck' or 'excavator' is detected, label it as 'CONSTRUCTION_VEHICLE_DETECTED' with 'CRITICAL' severity.
> 3. Save a frame of the detection locally and host it via a simple Python HTTP server.
> 4. Publish the detection as a JSON payload to a local Mosquitto MQTT broker on topic 'sentinel/alerts' following the RootsSecure standard (vendor_event_id, edge_event_type, logic_level, reason, media_ref, occurred_at)."

## 4. Hardware Heartbeat
Ensure the Pi also publishes a heartbeat every 30 seconds to `sentinel/heartbeat`:
```json
{
  "cpu_temp_c": 45.5,
  "network_latency_ms": 12,
  "power_status": "direct_power",
  "ram_usage_percent": 32.1,
  "storage_usage_percent": 15.0,
  "battery_percent": 100
}
```
