from __future__ import annotations

import hashlib
import os
import platform
from pathlib import Path


def get_device_seed() -> str:
    android_id = os.getenv("ANDROID_ID", "")
    machine_id = _read_text(Path("/etc/machine-id"))
    node = platform.node()
    payload = "|".join(part for part in [android_id, machine_id, node] if part)
    if not payload:
        payload = "fallback-mobile-device"
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def _read_text(path: Path) -> str | None:
    try:
        text = path.read_text(encoding="utf-8").strip()
        return text or None
    except OSError:
        return None
