from __future__ import annotations

import logging
from typing import Any

import requests

from .tls_pinning import verify_certificate_pin


class ApiError(RuntimeError):
    def __init__(self, status_code: int | None, message: str, retryable: bool = False) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.retryable = retryable


class ApiClient:
    def __init__(
        self,
        base_url: str,
        request_timeout_secs: float,
        cert_fingerprint_sha256: str,
        logger: logging.Logger,
    ) -> None:
        if not base_url.startswith("https://"):
            raise ValueError("Only HTTPS is allowed.")

        self._base_url = base_url.rstrip("/")
        self._timeout = request_timeout_secs
        self._fingerprint = cert_fingerprint_sha256
        self._logger = logger

        self._session = requests.Session()
        self._session.headers.update(
            {
                "Accept": "application/json",
                "Content-Type": "application/json",
                "User-Agent": "nri-plot-sentinel-mobile/1.0",
            }
        )

    def login(self, email: str, password: str) -> dict[str, Any]:
        return self._request("POST", "/api/v1/mobile/auth/login", payload={"email": email, "password": password})

    def refresh(self, refresh_token: str) -> dict[str, Any]:
        return self._request("POST", "/api/v1/mobile/auth/refresh", payload={"refresh_token": refresh_token})

    def fetch_dashboard(self, access_token: str) -> dict[str, Any]:
        return self._request("GET", "/api/v1/mobile/dashboard", access_token=access_token)

    def fetch_alerts(self, access_token: str) -> dict[str, Any]:
        return self._request("GET", "/api/v1/mobile/alerts", access_token=access_token)

    def fetch_devices(self, access_token: str) -> dict[str, Any]:
        return self._request("GET", "/api/v1/mobile/devices", access_token=access_token)

    def acknowledge_alert(self, access_token: str, alert_id: str) -> dict[str, Any]:
        return self._request(
            "POST",
            f"/api/v1/mobile/alerts/{alert_id}/acknowledge",
            access_token=access_token,
            payload={},
        )

    def _request(
        self,
        method: str,
        path: str,
        payload: dict[str, Any] | None = None,
        access_token: str | None = None,
    ) -> dict[str, Any]:
        if not verify_certificate_pin(self._base_url, self._fingerprint):
            raise ApiError(status_code=None, message="TLS certificate pin validation failed.", retryable=False)

        headers: dict[str, str] = {}
        if access_token:
            headers["Authorization"] = f"Bearer {access_token}"

        try:
            response = self._session.request(
                method=method,
                url=f"{self._base_url}{path}",
                headers=headers,
                json=payload,
                timeout=self._timeout,
            )
        except requests.RequestException as exc:
            raise ApiError(status_code=None, message=str(exc), retryable=True) from exc

        if response.status_code >= 400:
            msg = _extract_error(response)
            retryable = response.status_code in {408, 425, 429, 500, 502, 503, 504}
            raise ApiError(status_code=response.status_code, message=msg, retryable=retryable)

        if not response.content:
            return {}

        try:
            data = response.json()
            if isinstance(data, dict):
                return data
            return {"data": data}
        except ValueError:
            return {"raw": response.text}


def _extract_error(response: requests.Response) -> str:
    try:
        data = response.json()
    except ValueError:
        return response.text or f"HTTP {response.status_code}"

    if isinstance(data, dict):
        for key in ["message", "error", "detail"]:
            if key in data:
                return str(data[key])
        return str(data)
    return response.text or f"HTTP {response.status_code}"
