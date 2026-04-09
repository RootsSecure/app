from __future__ import annotations

import json
import os
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


def _env_bool(name: str, default: bool) -> bool:
    raw = os.getenv(name)
    if raw is None:
        return default
    return raw.strip().lower() in {"1", "true", "yes", "on"}


def _env_int(name: str, default: int) -> int:
    raw = os.getenv(name)
    if raw is None:
        return default
    return int(raw)


def _env_float(name: str, default: float) -> float:
    raw = os.getenv(name)
    if raw is None:
        return default
    return float(raw)


def _env_json_object(name: str, default: dict[str, Any] | None = None) -> dict[str, Any]:
    default = default or {}
    raw = os.getenv(name)
    if raw is None or not raw.strip():
        return default
    parsed = json.loads(raw)
    if not isinstance(parsed, dict):
        raise ValueError(f"{name} must be a JSON object.")
    return parsed


@dataclass(frozen=True)
class PiClientConfig:
    base_url: str
    provisioning_token: str
    hardware_id: str | None
    verify_tls: bool
    request_timeout_secs: float
    state_dir: Path
    session_state_path: Path
    queue_db_path: Path
    camera_events_path: Path
    log_level: str
    heartbeat_interval_secs: int
    event_flush_interval_secs: int
    event_flush_batch_size: int
    queue_max_backoff_secs: int
    connect_retry_base_secs: int
    connect_retry_max_secs: int
    session_expiry_skew_secs: int
    network_check_host: str
    network_check_port: int
    network_check_timeout_secs: float
    camera_poll_interval_secs: float
    connect_metadata: dict[str, Any] = field(default_factory=dict)
    heartbeat_metadata: dict[str, Any] = field(default_factory=dict)

    def validate(self) -> None:
        if not self.base_url:
            raise ValueError("GATEWAY_BASE_URL is required.")
        if not self.provisioning_token:
            raise ValueError("PI_PROVISIONING_TOKEN is required.")
        if self.request_timeout_secs <= 0:
            raise ValueError("PI_REQUEST_TIMEOUT_SECS must be > 0.")
        if self.heartbeat_interval_secs <= 0:
            raise ValueError("PI_HEARTBEAT_INTERVAL_SECS must be > 0.")
        if self.event_flush_interval_secs <= 0:
            raise ValueError("PI_EVENT_FLUSH_INTERVAL_SECS must be > 0.")
        if self.event_flush_batch_size <= 0:
            raise ValueError("PI_EVENT_FLUSH_BATCH_SIZE must be > 0.")
        if self.connect_retry_base_secs <= 0 or self.connect_retry_max_secs <= 0:
            raise ValueError("Connect retry values must be > 0.")
        if self.connect_retry_base_secs > self.connect_retry_max_secs:
            raise ValueError("PI_CONNECT_RETRY_BASE_SECS cannot exceed PI_CONNECT_RETRY_MAX_SECS.")
        if self.session_expiry_skew_secs < 0:
            raise ValueError("PI_SESSION_EXPIRY_SKEW_SECS must be >= 0.")


def load_config_from_env() -> PiClientConfig:
    state_dir = Path(os.getenv("PI_CLIENT_STATE_DIR", "./data")).resolve()
    session_state_path = Path(
        os.getenv("PI_SESSION_STATE_PATH", str(state_dir / "session_state.json"))
    ).resolve()
    queue_db_path = Path(
        os.getenv("PI_QUEUE_DB_PATH", str(state_dir / "event_queue.sqlite3"))
    ).resolve()
    camera_events_path = Path(
        os.getenv("PI_CAMERA_EVENTS_PATH", str(state_dir / "camera_events.jsonl"))
    ).resolve()

    config = PiClientConfig(
        base_url=os.getenv("GATEWAY_BASE_URL", "").rstrip("/"),
        provisioning_token=os.getenv("PI_PROVISIONING_TOKEN", ""),
        hardware_id=os.getenv("PI_HARDWARE_ID"),
        verify_tls=_env_bool("PI_VERIFY_TLS", True),
        request_timeout_secs=_env_float("PI_REQUEST_TIMEOUT_SECS", 10.0),
        state_dir=state_dir,
        session_state_path=session_state_path,
        queue_db_path=queue_db_path,
        camera_events_path=camera_events_path,
        log_level=os.getenv("PI_LOG_LEVEL", "INFO").upper(),
        heartbeat_interval_secs=_env_int("PI_HEARTBEAT_INTERVAL_SECS", 60),
        event_flush_interval_secs=_env_int("PI_EVENT_FLUSH_INTERVAL_SECS", 5),
        event_flush_batch_size=_env_int("PI_EVENT_FLUSH_BATCH_SIZE", 25),
        queue_max_backoff_secs=_env_int("PI_QUEUE_MAX_BACKOFF_SECS", 300),
        connect_retry_base_secs=_env_int("PI_CONNECT_RETRY_BASE_SECS", 2),
        connect_retry_max_secs=_env_int("PI_CONNECT_RETRY_MAX_SECS", 60),
        session_expiry_skew_secs=_env_int("PI_SESSION_EXPIRY_SKEW_SECS", 30),
        network_check_host=os.getenv("PI_NETWORK_CHECK_HOST", "1.1.1.1"),
        network_check_port=_env_int("PI_NETWORK_CHECK_PORT", 443),
        network_check_timeout_secs=_env_float("PI_NETWORK_CHECK_TIMEOUT_SECS", 2.0),
        camera_poll_interval_secs=_env_float("PI_CAMERA_POLL_INTERVAL_SECS", 1.0),
        connect_metadata=_env_json_object("PI_CONNECT_METADATA_JSON", {}),
        heartbeat_metadata=_env_json_object("PI_HEARTBEAT_METADATA_JSON", {}),
    )
    config.validate()
    return config
