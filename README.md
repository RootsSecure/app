# NRI Plot Sentinel Raspberry Pi Gateway Client

Secure Raspberry Pi service for remote vacant-property monitoring. The client bootstraps with a provisioning token, obtains a gateway session token, sends periodic heartbeats, and uploads normalized camera events with offline buffering.

## What This Service Does

- Uses dedicated gateway endpoints only:
  - `POST /api/v1/gateway/raspberry-pi/connect`
  - `POST /api/v1/gateway/raspberry-pi/devices/{device_id}/heartbeat`
  - `POST /api/v1/gateway/raspberry-pi/devices/{device_id}/events`
- Uses provisioning token only for bootstrap/reconnect.
- Stores session token + device ID locally with restrictive file permissions (Linux chmod `600`).
- Reconnects automatically when token expires or API returns `401`.
- Sends fixed-interval heartbeat with network status, power status, battery level, and metadata.
- Buffers unsent events in a local SQLite queue during connectivity issues and retries with backoff.
- Keeps modules separated for camera detection source, event classification, token management, queueing, and HTTP transport.

## Project Layout

- `src/pi_client/session_manager.py` bootstrap and reconnect logic.
- `src/pi_client/heartbeat_sender.py` heartbeat loop.
- `src/pi_client/event_uploader.py` event delivery and queue draining.
- `src/pi_client/event_queue.py` local offline queue.
- `src/pi_client/camera_adapter.py` detection source adapter.
- `src/pi_client/event_classifier.py` normalization into backend schema.
- `src/pi_client/api_transport.py` HTTPS transport with timeouts and structured errors.

## Setup

1. Install dependencies.

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

2. Configure environment.

```bash
cp .env.example .env
# edit .env with real API base URL and provisioning token
```

3. Run service manually.

```bash
PYTHONPATH=src python -m pi_client.main
```

## Registering the Pi with Backend

1. Set `PI_PROVISIONING_TOKEN` and `GATEWAY_BASE_URL` in `.env`.
2. Ensure `PI_HARDWARE_ID` is set or let the service auto-derive a stable one.
3. Start the service. It calls `POST /connect` and stores returned session state at `PI_SESSION_STATE_PATH`.
4. After connect succeeds, heartbeat and events use the returned session token only.

## Camera Detection Input Contract

The default adapter tails `PI_CAMERA_EVENTS_PATH` (`jsonl`). Append one JSON object per line from your camera detector process.

Example line:

```json
{"vendor_event_id":"cam-evt-1001","occurred_at":"2026-04-08T12:30:00Z","event_type":"person","confidence":0.94,"metadata":{"zone":"gate"},"media_refs":["/var/lib/nri-plot-sentinel-pi/media/frame-1001.jpg"]}
```

The classifier normalizes this to backend event fields:

- `alert_type`
- `vendor_event_id`
- `occurred_at`
- `metadata_json`
- `media_refs`

## Running as systemd Service (Raspberry Pi)

```bash
sudo cp deploy/nri-plot-sentinel-pi.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable nri-plot-sentinel-pi
sudo systemctl start nri-plot-sentinel-pi
sudo systemctl status nri-plot-sentinel-pi
```

Make sure:

- `WorkingDirectory` and `EnvironmentFile` in the unit match your install path.
- `.env` has production values.
- outbound HTTPS to the gateway is allowed.

## Notes

- `camera_capture.py` contains an optional `PiCameraCapture` wrapper using `picamera2` when you need local snapshot capture.
- `JsonlCameraAdapter` intentionally decouples detection from transport, so you can plug in OpenCV, Edge TPU, or another detector process without changing gateway logic.
