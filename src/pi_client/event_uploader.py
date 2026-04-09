from __future__ import annotations

import logging
import threading
from typing import Any

from .api_transport import ApiError, GatewayApiClient
from .event_queue import EventQueue
from .models import GatewayEvent
from .session_manager import SessionManager


class EventUploader:
    def __init__(
        self,
        api_client: GatewayApiClient,
        session_manager: SessionManager,
        queue: EventQueue,
        flush_interval_secs: int,
        flush_batch_size: int,
        max_backoff_secs: int,
        logger: logging.Logger,
    ) -> None:
        self._api_client = api_client
        self._session_manager = session_manager
        self._queue = queue
        self._flush_interval_secs = flush_interval_secs
        self._flush_batch_size = flush_batch_size
        self._max_backoff_secs = max_backoff_secs
        self._logger = logger

        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._stop_event.clear()
        self._thread = threading.Thread(target=self._run, name="event-uploader", daemon=True)
        self._thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        if self._thread:
            self._thread.join(timeout=5)

    def enqueue_event(self, event: GatewayEvent) -> None:
        inserted = self._queue.enqueue(event)
        if inserted:
            self._logger.info(
                "Event queued.",
                extra={"vendor_event_id": event.vendor_event_id, "alert_type": event.alert_type},
            )

    def flush_once(self) -> None:
        items = self._queue.fetch_ready(self._flush_batch_size)
        for item in items:
            sent, error = self._send_with_refresh(item.payload)
            if sent:
                self._queue.mark_sent(item.row_id)
                continue

            attempt = item.attempts + 1
            delay = min(2 ** min(attempt, 8), self._max_backoff_secs)
            self._queue.mark_retry(item.row_id, delay_secs=delay, error=error)

    def _run(self) -> None:
        while not self._stop_event.wait(self._flush_interval_secs):
            try:
                self.flush_once()
            except Exception as exc:
                self._logger.exception("Unexpected uploader loop error.", extra={"error": str(exc)})

    def _send_with_refresh(self, payload: dict[str, Any]) -> tuple[bool, str]:
        session = self._session_manager.get_session()

        try:
            self._api_client.upload_event(session.session_token, session.device_id, payload)
            self._logger.info(
                "Event delivered.",
                extra={"vendor_event_id": payload.get("vendor_event_id"), "device_id": session.device_id},
            )
            return True, ""
        except ApiError as exc:
            if exc.status_code == 401:
                self._logger.warning("Event upload got 401, refreshing session token.")
                refreshed = self._session_manager.handle_unauthorized(previous_token=session.session_token)
                try:
                    self._api_client.upload_event(refreshed.session_token, refreshed.device_id, payload)
                    self._logger.info(
                        "Event delivered after reconnect.",
                        extra={
                            "vendor_event_id": payload.get("vendor_event_id"),
                            "device_id": refreshed.device_id,
                        },
                    )
                    return True, ""
                except ApiError as retry_exc:
                    self._logger.warning(
                        "Event upload failed after reconnect.",
                        extra={"status_code": retry_exc.status_code, "error": str(retry_exc)},
                    )
                    return False, str(retry_exc)

            self._logger.warning(
                "Event upload failed.",
                extra={"status_code": exc.status_code, "retryable": exc.retryable, "error": str(exc)},
            )
            return False, str(exc)
        except Exception as exc:  # pragma: no cover
            self._logger.exception("Unexpected event upload error.", extra={"error": str(exc)})
            return False, str(exc)
