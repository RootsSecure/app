from __future__ import annotations

import json
import sqlite3
import threading
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any


@dataclass
class QueuedAction:
    row_id: int
    action_type: str
    payload: dict[str, Any]
    attempts: int


class OfflineActionQueue:
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
                CREATE TABLE IF NOT EXISTS offline_actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    action_type TEXT NOT NULL,
                    payload_json TEXT NOT NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    next_attempt_at TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """
            )

    def enqueue(self, action_type: str, payload: dict[str, Any]) -> None:
        now = datetime.now(timezone.utc).isoformat()
        payload_json = json.dumps(payload)
        with self._lock, self._conn:
            self._conn.execute(
                """
                INSERT INTO offline_actions (action_type, payload_json, attempts, next_attempt_at, created_at)
                VALUES (?, ?, 0, ?, ?)
                """,
                (action_type, payload_json, now, now),
            )

    def fetch_ready(self, limit: int) -> list[QueuedAction]:
        now = datetime.now(timezone.utc).isoformat()
        with self._lock, self._conn:
            rows = self._conn.execute(
                """
                SELECT id, action_type, payload_json, attempts
                FROM offline_actions
                WHERE next_attempt_at <= ?
                ORDER BY created_at ASC
                LIMIT ?
                """,
                (now, limit),
            ).fetchall()

        result: list[QueuedAction] = []
        for row in rows:
            result.append(
                QueuedAction(
                    row_id=int(row["id"]),
                    action_type=str(row["action_type"]),
                    payload=json.loads(row["payload_json"]),
                    attempts=int(row["attempts"]),
                )
            )
        return result

    def mark_done(self, row_id: int) -> None:
        with self._lock, self._conn:
            self._conn.execute("DELETE FROM offline_actions WHERE id = ?", (row_id,))

    def mark_retry(self, row_id: int, attempts: int) -> None:
        delay = min(2 ** min(attempts, 8), 300)
        next_attempt_at = (datetime.now(timezone.utc) + timedelta(seconds=delay)).isoformat()
        with self._lock, self._conn:
            self._conn.execute(
                """
                UPDATE offline_actions
                SET attempts = attempts + 1,
                    next_attempt_at = ?
                WHERE id = ?
                """,
                (next_attempt_at, row_id),
            )

    def size(self) -> int:
        with self._lock, self._conn:
            row = self._conn.execute("SELECT COUNT(*) AS total FROM offline_actions").fetchone()
            return int(row["total"])

    def close(self) -> None:
        with self._lock:
            self._conn.close()
