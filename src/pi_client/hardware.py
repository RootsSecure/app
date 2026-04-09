from __future__ import annotations

import hashlib
import os
import platform
from pathlib import Path


def _read_text(path: Path) -> str | None:
    try:
        value = path.read_text(encoding="utf-8").strip()
        return value or None
    except OSError:
        return None


def _first_mac_address() -> str | None:
    net_path = Path("/sys/class/net")
    if not net_path.exists():
        return None

    for iface in sorted(net_path.iterdir()):
        if iface.name == "lo":
            continue
        address = _read_text(iface / "address")
        if address:
            return address
    return None


def get_stable_hardware_id(configured_hardware_id: str | None) -> str:
    if configured_hardware_id:
        return configured_hardware_id.strip()

    machine_id = _read_text(Path("/etc/machine-id"))
    mac_address = _first_mac_address()
    node_name = platform.node()

    seed = "|".join(
        value for value in [machine_id, mac_address, node_name, os.getenv("HOSTNAME")] if value
    )
    if not seed:
        seed = "unknown-device"

    digest = hashlib.sha256(seed.encode("utf-8")).hexdigest()
    return f"pi-{digest[:32]}"
