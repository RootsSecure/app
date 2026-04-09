from __future__ import annotations

import base64
import hashlib
import json
import os
from pathlib import Path
from typing import Any

from cryptography.fernet import Fernet, InvalidToken

from .device_seed import get_device_seed


class SecureStore:
    def __init__(self, state_path: Path, app_secret: str) -> None:
        self._state_path = state_path
        self._app_secret = app_secret

    def save_tokens(self, payload: dict[str, Any]) -> None:
        self._state_path.parent.mkdir(parents=True, exist_ok=True)
        key = self._derive_key()
        token = Fernet(key).encrypt(json.dumps(payload).encode("utf-8"))

        temp_path = self._state_path.with_suffix(".tmp")
        temp_path.write_bytes(token)
        os.replace(temp_path, self._state_path)
        self._secure_permissions(self._state_path)

    def load_tokens(self) -> dict[str, Any] | None:
        if not self._state_path.exists():
            return None

        key = self._derive_key()
        encrypted = self._state_path.read_bytes()
        try:
            raw = Fernet(key).decrypt(encrypted)
        except InvalidToken:
            return None
        return json.loads(raw.decode("utf-8"))

    def clear(self) -> None:
        try:
            self._state_path.unlink(missing_ok=True)
        except OSError:
            return

    def _derive_key(self) -> bytes:
        device_seed = get_device_seed()
        digest = hashlib.sha256(f"{device_seed}:{self._app_secret}".encode("utf-8")).digest()
        return base64.urlsafe_b64encode(digest)

    @staticmethod
    def _secure_permissions(path: Path) -> None:
        if os.name == "nt":
            return
        try:
            os.chmod(path, 0o600)
        except OSError:
            return
