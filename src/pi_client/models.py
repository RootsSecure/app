from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any


@dataclass
class SessionInfo:
    session_token: str
    device_id: str
    expires_at: datetime | None = None
    obtained_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))


@dataclass
class RawDetection:
    vendor_event_id: str
    occurred_at: datetime
    event_type: str
    metadata: dict[str, Any] = field(default_factory=dict)
    media_refs: list[str] = field(default_factory=list)
    confidence: float | None = None

    @classmethod
    def from_mapping(cls, payload: dict[str, Any]) -> "RawDetection":
        vendor_event_id = str(payload["vendor_event_id"])

        raw_occurred_at = payload.get("occurred_at")
        occurred_at = datetime.now(timezone.utc)
        if isinstance(raw_occurred_at, str) and raw_occurred_at.strip():
            normalized = raw_occurred_at.replace("Z", "+00:00")
            occurred_at = datetime.fromisoformat(normalized)

        event_type = str(payload.get("event_type") or payload.get("label") or "unknown")

        metadata = payload.get("metadata")
        if not isinstance(metadata, dict):
            metadata = {}

        media_refs = payload.get("media_refs")
        if not isinstance(media_refs, list):
            media_refs = []

        confidence = payload.get("confidence")
        if confidence is not None:
            try:
                confidence = float(confidence)
            except (TypeError, ValueError):
                confidence = None

        return cls(
            vendor_event_id=vendor_event_id,
            occurred_at=occurred_at,
            event_type=event_type,
            metadata=metadata,
            media_refs=[str(item) for item in media_refs],
            confidence=confidence,
        )


@dataclass
class GatewayEvent:
    alert_type: str
    vendor_event_id: str
    occurred_at: datetime
    metadata_json: dict[str, Any]
    media_refs: list[str]

    def to_payload(self) -> dict[str, Any]:
        return {
            "alert_type": self.alert_type,
            "vendor_event_id": self.vendor_event_id,
            "occurred_at": self.occurred_at.astimezone(timezone.utc).isoformat(),
            "metadata_json": self.metadata_json,
            "media_refs": self.media_refs,
        }
