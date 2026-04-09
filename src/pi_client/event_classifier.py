from __future__ import annotations

from typing import Any

from .models import GatewayEvent, RawDetection


class DetectionClassifier:
    def __init__(self, custom_mapping: dict[str, str] | None = None) -> None:
        self._mapping = {
            "person": "unauthorized_presence",
            "human": "unauthorized_presence",
            "motion": "suspicious_activity",
            "intrusion": "intrusion_detected",
            "vehicle": "vehicle_detected",
            "tamper": "tamper_attempt",
            "construction": "illegal_construction",
        }
        if custom_mapping:
            self._mapping.update({k.lower(): v for k, v in custom_mapping.items()})

    def normalize(self, detection: RawDetection) -> GatewayEvent:
        raw_type = detection.event_type.lower().strip()
        alert_type = self._mapping.get(raw_type, "suspicious_activity")

        metadata_json: dict[str, Any] = dict(detection.metadata)
        metadata_json["raw_event_type"] = detection.event_type
        if detection.confidence is not None:
            metadata_json["confidence"] = detection.confidence

        return GatewayEvent(
            alert_type=alert_type,
            vendor_event_id=detection.vendor_event_id,
            occurred_at=detection.occurred_at,
            metadata_json=metadata_json,
            media_refs=list(detection.media_refs),
        )
