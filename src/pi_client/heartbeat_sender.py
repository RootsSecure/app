from __future__ import annotations

import logging
import socket
import threading
from typing import Any

from .api_transport import ApiError, GatewayApiClient
from .event_queue import EventQueue
from .session_manager import SessionManager

try:
    import psutil
except ImportError:  # pragma: no cover
    psutil = None  # type: ignore


class HeartbeatSender:
    def __init__(
        self,
        api_client: GatewayApiClient,
        session_manager: SessionManager,
        queue: EventQueue,
        interval_secs: int,
        network_host: str,
        network_port: int,
        network_timeout_secs: float,
        base_metadata: dict[str, Any],
        logger: logging.Logger,
    ) -> None:
        self._api_client = api_client
        self._session_manager = session_manager
        self._queue = queue
        self._interval_secs = interval_secs
        self._network_host = network_host
        self._network_port = network_port
        self._network_timeout_secs = network_timeout_secs
        self._base_metadata = base_metadata
        self._logger = logger

        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._stop_event.clear()
        self._thread = threading.Thread(target=self._run, name="heartbeat-sender", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        if self._thread:
            self._thread.join(timeout=5)

    def _run(self) -> None:
        while not self._stop_event.wait(self._interval_secs):
            try:
                self.send_once()
            except Exception as exc:
                self._logger.exception("Unexpected heartbeat loop error.", extra={"error": str(exc)})

    def send_once(self) -> None:
        payload = {
            "network_status": self._network_status(),
            "power_status": self._power_status(),
            "battery_level": self._battery_level(),
            "metadata": self._heartbeat_metadata(),
        }

        session = self._session_manager.get_session()

        try:
            self._api_client.send_heartbeat(session.session_token, session.device_id, payload)
            self._logger.info("Heartbeat sent.", extra={"device_id": session.device_id})
            return
        except ApiError as exc:
            if exc.status_code == 401:
                self._logger.warning("Heartbeat got 401, refreshing session token.")
                refreshed = self._session_manager.handle_unauthorized(previous_token=session.session_token)
                self._api_client.send_heartbeat(refreshed.session_token, refreshed.device_id, payload)
                self._logger.info(
                    "Heartbeat sent after reconnect.",
                    extra={"device_id": refreshed.device_id},
                )
                return

            self._logger.warning(
                "Heartbeat failed.",
                extra={"status_code": exc.status_code, "retryable": exc.retryable, "error": str(exc)},
            )

    def _heartbeat_metadata(self) -> dict[str, Any]:
        metadata = dict(self._base_metadata)
        metadata["queued_events"] = self._queue.size()
        return metadata

    def _network_status(self) -> str:
        try:
            with socket.create_connection(
                (self._network_host, self._network_port),
                timeout=self._network_timeout_secs,
            ):
                return "online"
        except OSError:
            return "offline"

    @staticmethod
    def _power_status() -> str:
        if psutil is None:
            return "unknown"
        battery = psutil.sensors_battery()
        if battery is None:
            return "unknown"
        return "charging" if battery.power_plugged else "battery"

    @staticmethod
    def _battery_level() -> float | None:
        if psutil is None:
            return None
        battery = psutil.sensors_battery()
        if battery is None:
            return None
        try:
            return float(battery.percent)
        except (TypeError, ValueError):
            return None
