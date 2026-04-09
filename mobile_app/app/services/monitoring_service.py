from __future__ import annotations

import logging
import threading
from dataclasses import dataclass, field
from typing import Any

from app.network.api_client import ApiError

from ..network.auth_manager import AuthManager
from ..network.offline_queue import OfflineActionQueue


@dataclass
class MobileState:
    dashboard: dict[str, Any] = field(default_factory=dict)
    alerts: list[dict[str, Any]] = field(default_factory=list)
    devices: list[dict[str, Any]] = field(default_factory=list)
    last_error: str = ""


class MonitoringService:
    def __init__(
        self,
        auth_manager: AuthManager,
        api_client,
        offline_queue: OfflineActionQueue,
        poll_interval_secs: int,
        logger: logging.Logger,
    ) -> None:
        self._auth = auth_manager
        self._api = api_client
        self._queue = offline_queue
        self._poll_interval = poll_interval_secs
        self._logger = logger

        self._state_lock = threading.Lock()
        self._state = MobileState()
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._stop_event.clear()
        self._thread = threading.Thread(target=self._run, name="monitoring-service", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        if self._thread:
            self._thread.join(timeout=5)

    def snapshot(self) -> MobileState:
        with self._state_lock:
            return MobileState(
                dashboard=dict(self._state.dashboard),
                alerts=[dict(item) for item in self._state.alerts],
                devices=[dict(item) for item in self._state.devices],
                last_error=self._state.last_error,
            )

    def acknowledge_alert(self, alert_id: str) -> None:
        try:
            self._auth.run_with_auth(lambda t: self._api.acknowledge_alert(t, alert_id))
        except Exception:
            self._queue.enqueue("ack_alert", {"alert_id": alert_id})

    def _run(self) -> None:
        while not self._stop_event.wait(self._poll_interval):
            self.refresh_once()
            self.flush_queue_once()

    def refresh_once(self) -> None:
        try:
            dashboard = self._auth.run_with_auth(self._api.fetch_dashboard)
            alerts_payload = self._auth.run_with_auth(self._api.fetch_alerts)
            devices_payload = self._auth.run_with_auth(self._api.fetch_devices)

            alerts = alerts_payload.get("alerts")
            if not isinstance(alerts, list):
                alerts = []

            devices = devices_payload.get("devices")
            if not isinstance(devices, list):
                devices = []

            with self._state_lock:
                self._state.dashboard = dashboard
                self._state.alerts = alerts
                self._state.devices = devices
                self._state.last_error = ""
        except Exception as exc:
            self._logger.warning("Refresh failed.", extra={"error": str(exc)})
            with self._state_lock:
                self._state.last_error = str(exc)

    def flush_queue_once(self) -> None:
        items = self._queue.fetch_ready(limit=20)
        for item in items:
            try:
                if item.action_type == "ack_alert":
                    alert_id = str(item.payload.get("alert_id") or "")
                    if not alert_id:
                        self._queue.mark_done(item.row_id)
                        continue
                    self._auth.run_with_auth(lambda t: self._api.acknowledge_alert(t, alert_id))
                    self._queue.mark_done(item.row_id)
                else:
                    self._queue.mark_done(item.row_id)
            except ApiError as exc:
                if exc.retryable or exc.status_code in {401, 429, 500, 502, 503, 504, None}:
                    self._queue.mark_retry(item.row_id, attempts=item.attempts + 1)
                else:
                    self._queue.mark_done(item.row_id)
            except Exception:
                self._queue.mark_retry(item.row_id, attempts=item.attempts + 1)
