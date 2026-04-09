from __future__ import annotations

import json
import logging
import os
import threading
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any

from .api_transport import ApiError, GatewayApiClient
from .models import SessionInfo


class SessionManager:
    def __init__(
        self,
        api_client: GatewayApiClient,
        provisioning_token: str,
        hardware_id: str,
        state_path: Path,
        connect_metadata: dict[str, Any],
        expiry_skew_secs: int,
        connect_retry_base_secs: int,
        connect_retry_max_secs: int,
        logger: logging.Logger,
    ) -> None:
        self._api_client = api_client
        self._provisioning_token = provisioning_token
        self._hardware_id = hardware_id
        self._state_path = state_path
        self._connect_metadata = connect_metadata
        self._expiry_skew = timedelta(seconds=expiry_skew_secs)
        self._connect_retry_base_secs = connect_retry_base_secs
        self._connect_retry_max_secs = connect_retry_max_secs
        self._logger = logger

        self._lock = threading.RLock()
        self._session: SessionInfo | None = self._load_state()

    def get_session(self, force_refresh: bool = False) -> SessionInfo:
        with self._lock:
            if force_refresh or self._session is None or self._is_expired(self._session):
                self._session = self._connect_with_retry_locked()
                self._save_state(self._session)
            return self._session

    def handle_unauthorized(self, previous_token: str | None = None) -> SessionInfo:
        with self._lock:
            if previous_token and self._session and self._session.session_token != previous_token:
                return self._session
            self._session = self._connect_with_retry_locked()
            self._save_state(self._session)
            return self._session

    def _connect_with_retry_locked(self) -> SessionInfo:
        delay = self._connect_retry_base_secs
        while True:
            try:
                return self._connect_once_locked()
            except ApiError as exc:
                if not exc.retryable and exc.status_code not in {401, 403}:
                    raise
                self._logger.warning(
                    "Gateway connect failed, retrying.",
                    extra={
                        "status_code": exc.status_code,
                        "retry_delay_secs": delay,
                        "error": str(exc),
                    },
                )
                _sleep_interruptible(delay)
                delay = min(delay * 2, self._connect_retry_max_secs)

    def _connect_once_locked(self) -> SessionInfo:
        payload = {
            "hardware_id": self._hardware_id,
            "metadata": self._connect_metadata,
        }
        response = self._api_client.connect(self._provisioning_token, payload)

        session_token = _pick_str(
            response,
            ["session_token", "sessionToken", "token", "access_token", "accessToken"],
        )
        device_id = _pick_str(response, ["device_id", "deviceId", "id", "gateway_device_id"])

        if not session_token:
            raise RuntimeError("Connect response missing session token.")
        if not device_id:
            raise RuntimeError("Connect response missing device_id.")

        expires_at = _parse_expiry(response)

        self._logger.info(
            "Gateway session established.",
            extra={"device_id": device_id, "expires_at": expires_at.isoformat() if expires_at else None},
        )
        return SessionInfo(session_token=session_token, device_id=device_id, expires_at=expires_at)

    def _is_expired(self, session: SessionInfo) -> bool:
        if session.expires_at is None:
            return False
        now = datetime.now(timezone.utc)
        return now + self._expiry_skew >= session.expires_at

    def _load_state(self) -> SessionInfo | None:
        if not self._state_path.exists():
            return None

        try:
            data = json.loads(self._state_path.read_text(encoding="utf-8"))
        except (OSError, ValueError) as exc:
            self._logger.warning("Failed to load session state.", extra={"error": str(exc)})
            return None

        session_token = str(data.get("session_token") or "")
        device_id = str(data.get("device_id") or "")
        if not session_token or not device_id:
            return None

        expires_at = None
        raw_expires_at = data.get("expires_at")
        if isinstance(raw_expires_at, str) and raw_expires_at.strip():
            try:
                expires_at = datetime.fromisoformat(raw_expires_at.replace("Z", "+00:00"))
            except ValueError:
                expires_at = None

        session = SessionInfo(
            session_token=session_token,
            device_id=device_id,
            expires_at=expires_at,
        )

        if self._is_expired(session):
            return None
        return session

    def _save_state(self, session: SessionInfo) -> None:
        self._state_path.parent.mkdir(parents=True, exist_ok=True)
        _set_secure_permissions(self._state_path.parent, is_dir=True)

        payload = {
            "session_token": session.session_token,
            "device_id": session.device_id,
            "expires_at": session.expires_at.isoformat() if session.expires_at else None,
            "saved_at": datetime.now(timezone.utc).isoformat(),
        }

        temp_path = self._state_path.with_suffix(".tmp")
        temp_path.write_text(json.dumps(payload), encoding="utf-8")
        os.replace(temp_path, self._state_path)
        _set_secure_permissions(self._state_path, is_dir=False)


def _pick_str(payload: dict[str, Any], keys: list[str]) -> str | None:
    for key in keys:
        value = payload.get(key)
        if value is None:
            continue
        text = str(value).strip()
        if text:
            return text
    return None


def _parse_expiry(payload: dict[str, Any]) -> datetime | None:
    raw_expires_at = payload.get("expires_at") or payload.get("expiresAt")
    if isinstance(raw_expires_at, str) and raw_expires_at.strip():
        try:
            return datetime.fromisoformat(raw_expires_at.replace("Z", "+00:00"))
        except ValueError:
            pass

    raw_expires_in = payload.get("expires_in") or payload.get("expiresIn")
    if raw_expires_in is not None:
        try:
            seconds = int(raw_expires_in)
            return datetime.now(timezone.utc) + timedelta(seconds=seconds)
        except (TypeError, ValueError):
            pass

    return None


def _set_secure_permissions(path: Path, is_dir: bool) -> None:
    if os.name == "nt":
        return

    mode = 0o700 if is_dir else 0o600
    try:
        os.chmod(path, mode)
    except OSError:
        return


def _sleep_interruptible(seconds: int) -> None:
    # Local helper keeps retry loop simple without introducing additional threading primitives.
    import time

    time.sleep(seconds)
