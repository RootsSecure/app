from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class AppConfig:
    base_url: str
    cert_fingerprint_sha256: str
    request_timeout_secs: float
    poll_interval_secs: int
    token_state_path: Path
    queue_db_path: Path
    app_secret: str

    def validate(self) -> None:
        if not self.base_url.startswith("https://"):
            raise ValueError("API base URL must use HTTPS.")
        if self.request_timeout_secs <= 0:
            raise ValueError("Request timeout must be > 0.")
        if self.poll_interval_secs <= 0:
            raise ValueError("Poll interval must be > 0.")
        if not self.app_secret:
            raise ValueError("APP_SECRET is required.")


def load_config() -> AppConfig:
    root = Path(os.getenv("APP_STATE_DIR", "./mobile_state")).resolve()
    token_state_path = Path(os.getenv("TOKEN_STATE_PATH", str(root / "tokens.enc"))).resolve()
    queue_db_path = Path(os.getenv("QUEUE_DB_PATH", str(root / "offline_queue.sqlite3"))).resolve()

    cfg = AppConfig(
        base_url=os.getenv("API_BASE_URL", "https://api.example.com").rstrip("/"),
        cert_fingerprint_sha256=os.getenv("API_CERT_FINGERPRINT_SHA256", "").lower().replace(":", ""),
        request_timeout_secs=float(os.getenv("REQUEST_TIMEOUT_SECS", "10")),
        poll_interval_secs=int(os.getenv("POLL_INTERVAL_SECS", "20")),
        token_state_path=token_state_path,
        queue_db_path=queue_db_path,
        app_secret=os.getenv("APP_SECRET", "change-this-in-production"),
    )
    cfg.validate()
    return cfg
