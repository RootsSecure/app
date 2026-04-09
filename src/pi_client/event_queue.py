from __future__ import annotations

import json
import sqlite3
import threading
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any

from .models import GatewayEvent


@dataclass
class QueuedEvent:
    row_id: int
    payload: dict[str, Any]
    attempts: int


class EventQueue:
    def __init__(self, db_path: Path) -> None:
        self._db_path = db_path
        self._db_path.parent.mkdir(parents=True, exist_ok=True)

        self._conn = sqlite3.connect(str(db_path), check_same_thread=False)
        self._conn.row_factory = sqlite3.Row
        self._lock = threading.Lock()
        self._setup()

    def _setup(self) -> None:
        with self._conn:
            self._conn.execute("PRAGMA journal_mode=WAL")
            self._conn.execute(
                """
                CREATE TABLE IF NOT EXISTS queued_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vendor_event_id TEXT NOT NULL UNIQUE,
                    payload_json TEXT NOT NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    next_attempt_at TEXT NOT NULL,
                    last_error TEXT,
                    created_at TEXT NOT NULL
                )
                """
            )

    def enqueue(self, event: GatewayEvent) -> bool:
        payload_json = json.dumps(event.to_payload(), default=str)
        now = datetime.now(timezone.utc).isoformat()

        with self._lock, self._conn:
            cursor = self._conn.execute(
                """
                INSERT OR IGNORE INTO queued_events (
                    vendor_event_id,
                    payload_json,
                    attempts,
                    next_attempt_at,
                    created_at
                ) VALUES (?, ?, 0, ?, ?)
                """,
                (event.vendor_event_id, payload_json, now, now),
            )
            return cursor.rowcount > 0

    def fetch_ready(self, limit: int) -> list[QueuedEvent]:
        now = datetime.now(timezone.utc).isoformat()
        with self._lock, self._conn:
            rows = self._conn.execute(
                """
                SELECT id, payload_json, attempts
                FROM queued_events
                WHERE next_attempt_at <= ?
                ORDER BY created_at ASC
                LIMIT ?
                """,
                (now, limit),
            ).fetchall()

        items: list[QueuedEvent] = []
        for row in rows:
            payload = json.loads(row["payload_json"])
            items.append(
                QueuedEvent(
                    row_id=int(row["id"]),
                    payload=payload,
                    attempts=int(row["attempts"]),
                )
            )
        return items

    def mark_sent(self, row_id: int) -> None:
        with self._lock, self._conn:
            self._conn.execute("DELETE FROM queued_events WHERE id = ?", (row_id,))

    def mark_retry(self, row_id: int, delay_secs: int, error: str) -> None:
        next_attempt_at = (datetime.now(timezone.utc) + timedelta(seconds=delay_secs)).isoformat()
        with self._lock, self._conn:
            self._conn.execute(
                """
                UPDATE queued_events
                SET attempts = attempts + 1,
                    next_attempt_at = ?,
                    last_error = ?
                WHERE id = ?
                """,
                (next_attempt_at, error[:1000], row_id),
            )

    def size(self) -> int:
        with self._lock, self._conn:
            row = self._conn.execute("SELECT COUNT(*) AS total FROM queued_events").fetchone()
        return int(row["total"])

    def close(self) -> None:
        with self._lock:
            self._conn.close()
