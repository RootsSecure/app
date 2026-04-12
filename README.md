# NRI Plot Sentinel App Workspace

This repository contains the non-web application pieces for the NRI Plot Sentinel system:

- `android_app/` Android client written in Kotlin
- `mobile_app/` Python mobile-oriented client prototype
- `src/pi_client/` Raspberry Pi gateway client
- `deploy/` deployment assets for the Pi service

The accidental Vite/React website files have been removed so this folder reflects the app workspace again.

## Raspberry Pi Client

The Pi client bootstraps with a provisioning token, obtains a gateway session token, sends periodic heartbeats, and uploads normalized camera events with offline buffering.

### Setup

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
set PYTHONPATH=src
python -m pi_client.main
```

Key modules:

- `src/pi_client/session_manager.py` bootstrap and reconnect logic
- `src/pi_client/heartbeat_sender.py` heartbeat loop
- `src/pi_client/event_uploader.py` event delivery and queue draining
- `src/pi_client/event_queue.py` local offline queue
- `src/pi_client/api_transport.py` HTTPS transport and retry boundaries

## Mobile Python App

The Python mobile prototype entry point is `mobile_app/main.py`. Install its dependencies with:

```bash
pip install -r mobile_app/requirements.txt
```

`Kivy` currently needs Python 3.13 or lower on Windows in this workspace.

## Android App

The Android client source is under `android_app/app/src/main/`.

Open `android_app/` in Android Studio or use a local Gradle installation to build it. This repository does not currently include a Gradle wrapper.

## Project Layout

```text
app/
|-- android_app/
|-- deploy/
|-- mobile_app/
|-- src/pi_client/
|-- .env.example
|-- pyproject.toml
`-- requirements.txt
```
