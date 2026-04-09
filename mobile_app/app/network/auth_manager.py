from __future__ import annotations

import threading
from datetime import datetime, timedelta, timezone
from typing import Any

from app.security.secure_store import SecureStore

from .api_client import ApiClient, ApiError


class AuthManager:
    def __init__(self, api_client: ApiClient, secure_store: SecureStore) -> None:
        self._api_client = api_client
        self._secure_store = secure_store
        self._lock = threading.RLock()
        self._state: dict[str, Any] = self._secure_store.load_tokens() or {}

    def login(self, email: str, password: str) -> None:
        response = self._api_client.login(email=email, password=password)
        self._update_state_from_token_response(response)

    def logout(self) -> None:
        with self._lock:
            self._state = {}
            self._secure_store.clear()

    def access_token(self) -> str | None:
        with self._lock:
            if self._is_access_token_stale_locked():
                self._refresh_locked()
            token = self._state.get("access_token")
            return str(token) if token else None

    def run_with_auth(self, callback):
        token = self.access_token()
        if not token:
            raise RuntimeError("Not logged in")
        try:
            return callback(token)
        except ApiError as exc:
            if exc.status_code != 401:
                raise
            with self._lock:
                self._refresh_locked(force=True)
                retry_token = self._state.get("access_token")
            if not retry_token:
                raise RuntimeError("Session refresh failed") from exc
            return callback(str(retry_token))

    def _refresh_locked(self, force: bool = False) -> None:
        if not force and not self._is_access_token_stale_locked():
            return

        refresh_token = self._state.get("refresh_token")
        if not refresh_token:
            raise RuntimeError("No refresh token available")

        response = self._api_client.refresh(str(refresh_token))
        self._update_state_from_token_response(response)

    def _update_state_from_token_response(self, response: dict[str, Any]) -> None:
        access_token = str(response.get("access_token") or "")
        refresh_token = str(response.get("refresh_token") or "")
        expires_in = int(response.get("expires_in") or 900)

        if not access_token or not refresh_token:
            raise RuntimeError("Invalid auth response from backend")

        expires_at = datetime.now(timezone.utc) + timedelta(seconds=expires_in)
        state = {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "expires_at": expires_at.isoformat(),
        }

        with self._lock:
            self._state = state
            self._secure_store.save_tokens(state)

    def _is_access_token_stale_locked(self) -> bool:
        raw_expires_at = self._state.get("expires_at")
        if not raw_expires_at:
            return True
        try:
            expires_at = datetime.fromisoformat(str(raw_expires_at).replace("Z", "+00:00"))
        except ValueError:
            return True
        return datetime.now(timezone.utc) + timedelta(seconds=30) >= expires_at
