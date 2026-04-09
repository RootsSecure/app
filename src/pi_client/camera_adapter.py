from __future__ import annotations

import json
import logging
import time
from abc import ABC, abstractmethod
from pathlib import Path
from threading import Event
from typing import Iterator

from .models import RawDetection


class CameraDetectionSource(ABC):
    @abstractmethod
    def iter_detections(self, stop_event: Event) -> Iterator[RawDetection]:
        """Yield camera-side detections from the configured source."""


class JsonlCameraAdapter(CameraDetectionSource):
    """
    Reads newline-delimited JSON detections from a local file.

    This keeps capture and detection decoupled from gateway transport. A separate
    camera process can append records to this file.
    """

    def __init__(self, events_path: Path, poll_interval_secs: float, logger: logging.Logger) -> None:
        self._events_path = events_path
        self._poll_interval_secs = poll_interval_secs
        self._logger = logger

        self._events_path.parent.mkdir(parents=True, exist_ok=True)
        if not self._events_path.exists():
            self._events_path.write_text("", encoding="utf-8")

    def iter_detections(self, stop_event: Event) -> Iterator[RawDetection]:
        with self._events_path.open("r", encoding="utf-8") as handle:
            handle.seek(0, 2)

            while not stop_event.is_set():
                line = handle.readline()
                if not line:
                    time.sleep(self._poll_interval_secs)
                    continue

                stripped = line.strip()
                if not stripped:
                    continue

                try:
                    payload = json.loads(stripped)
                    if not isinstance(payload, dict):
                        raise ValueError("Detection line must be a JSON object.")
                    yield RawDetection.from_mapping(payload)
                except Exception as exc:
                    self._logger.warning(
                        "Skipping invalid camera detection record.",
                        extra={"error": str(exc), "line": stripped[:300]},
                    )
