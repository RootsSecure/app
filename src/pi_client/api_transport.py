from __future__ import annotations

import logging
from typing import Any

import requests


class ApiError(RuntimeError):
    def __init__(self, status_code: int | None, message: str, retryable: bool) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.retryable = retryable


class GatewayApiClient:
    def __init__(
        self,
        base_url: str,
        timeout_secs: float,
        verify_tls: bool,
        logger: logging.Logger,
    ) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout_secs = timeout_secs
        self._verify_tls = verify_tls
        self._logger = logger
        self._session = requests.Session()
        self._session.headers.update(
            {
                "Accept": "application/json",
                "Content-Type": "application/json",
                "User-Agent": "nri-plot-sentinel-pi/0.1.0",
            }
        )

        if not self._base_url.startswith("https://"):
            self._logger.warning(
                "Gateway URL is not HTTPS. This is not recommended for production.",
                extra={"base_url": self._base_url},
            )

    def connect(self, provisioning_token: str, payload: dict[str, Any]) -> dict[str, Any]:
        return self._request(
            method="POST",
            path="/api/v1/gateway/raspberry-pi/connect",
            bearer_token=provisioning_token,
            payload=payload,
        )

    def send_heartbeat(
        self,
        session_token: str,
        device_id: str,
        payload: dict[str, Any],
    ) -> dict[str, Any]:
        return self._request(
            method="POST",
            path=f"/api/v1/gateway/raspberry-pi/devices/{device_id}/heartbeat",
            bearer_token=session_token,
            payload=payload,
        )

    def upload_event(
        self,
        session_token: str,
        device_id: str,
        payload: dict[str, Any],
    ) -> dict[str, Any]:
        return self._request(
            method="POST",
            path=f"/api/v1/gateway/raspberry-pi/devices/{device_id}/events",
            bearer_token=session_token,
            payload=payload,
        )

    def _request(
        self,
        method: str,
        path: str,
        bearer_token: str,
        payload: dict[str, Any],
    ) -> dict[str, Any]:
        url = f"{self._base_url}{path}"
        headers = {"Authorization": f"Bearer {bearer_token}"}

        try:
            response = self._session.request(
                method=method,
                url=url,
                json=payload,
                headers=headers,
                timeout=self._timeout_secs,
                verify=self._verify_tls,
            )
        except requests.RequestException as exc:
            raise ApiError(status_code=None, message=str(exc), retryable=True) from exc

        if response.status_code >= 400:
            message = _response_text(response)
            retryable = response.status_code in {408, 409, 425, 429, 500, 502, 503, 504}
            raise ApiError(status_code=response.status_code, message=message, retryable=retryable)

        if not response.content:
            return {}

        try:
            data = response.json()
            if isinstance(data, dict):
                return data
            return {"data": data}
        except ValueError:
            return {"raw": response.text}


def _response_text(response: requests.Response) -> str:
    try:
        data = response.json()
        if isinstance(data, dict):
            if "message" in data:
                return str(data["message"])
            return str(data)
    except ValueError:
        pass
    return response.text or f"HTTP {response.status_code}"
