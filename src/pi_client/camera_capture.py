from __future__ import annotations

import logging
from abc import ABC, abstractmethod
from pathlib import Path


class CameraCapture(ABC):
    @abstractmethod
    def capture_snapshot(self) -> str:
        """Capture a snapshot and return file path."""


class PiCameraCapture(CameraCapture):
    def __init__(self, output_dir: Path, logger: logging.Logger) -> None:
        self._logger = logger
        self._output_dir = output_dir
        self._output_dir.mkdir(parents=True, exist_ok=True)

        self._camera = None
        try:
            from picamera2 import Picamera2  # type: ignore
        except ImportError as exc:
            raise RuntimeError(
                "picamera2 is not installed. Install it on Raspberry Pi for camera capture support."
            ) from exc

        self._camera = Picamera2()
        self._camera.start()
        self._logger.info("Pi Camera initialized.")

    def capture_snapshot(self) -> str:
        if self._camera is None:
            raise RuntimeError("Camera not initialized.")

        from datetime import datetime

        image_path = self._output_dir / f"capture-{datetime.utcnow().strftime('%Y%m%dT%H%M%S%f')}.jpg"
        self._camera.capture_file(str(image_path))
        return str(image_path)

    def close(self) -> None:
        if self._camera is not None:
            self._camera.stop()
            self._camera = None
