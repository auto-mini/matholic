from __future__ import annotations

import base64
import json
import os
import re
from dataclasses import dataclass
from pathlib import Path

from .config import SecretProtector

MAX_CSV_BYTES = 1024 * 1024
MAX_CSV_FILENAME_BYTES = 120
QUEUE_VERSION = 1
DELIVERY_ID = re.compile(r"^[0-9a-f]{32}$")


@dataclass
class PendingCsv:
    delivery_id: str
    filename: str
    payload: bytearray

    def clear_sensitive_data(self) -> None:
        self.payload[:] = b"\x00" * len(self.payload)


@dataclass(frozen=True)
class PendingCsvSnapshot:
    pending: PendingCsv | None
    confirmed_delivery_id: str | None


class PendingCsvStore:
    def __init__(self, path: Path, protector: SecretProtector) -> None:
        self.path = path
        self.protector = protector

    def load(self) -> PendingCsvSnapshot:
        if not self.path.exists():
            return PendingCsvSnapshot(None, None)
        value = json.loads(self.path.read_text(encoding="utf-8"))
        if value.get("version") != QUEUE_VERSION:
            raise ValueError("지원하지 않는 CSV 대기 상태입니다.")
        delivery_id = str(value.get("delivery_id", ""))
        if not DELIVERY_ID.fullmatch(delivery_id):
            raise ValueError("CSV 전달 식별자가 올바르지 않습니다.")
        state = value.get("state")
        if state == "confirmed":
            return PendingCsvSnapshot(None, delivery_id)
        if state != "pending":
            raise ValueError("CSV 대기 상태가 올바르지 않습니다.")
        filename = self._validate_filename(str(value.get("filename", "")))
        try:
            protected = base64.urlsafe_b64decode(value["payload_protected"] + "==")
            payload = bytearray(self.protector.unprotect(protected))
        except (KeyError, ValueError) as error:
            raise ValueError("보호된 CSV 대기 파일을 읽지 못했습니다.") from error
        try:
            self._validate_payload(payload)
            return PendingCsvSnapshot(PendingCsv(delivery_id, filename, payload), None)
        except Exception:
            payload[:] = b"\x00" * len(payload)
            raise

    def save_pending(self, pending: PendingCsv) -> None:
        filename = self._validate_filename(pending.filename)
        self._validate_payload(pending.payload)
        if not DELIVERY_ID.fullmatch(pending.delivery_id):
            raise ValueError("CSV 전달 식별자가 올바르지 않습니다.")
        protected = self.protector.protect(bytes(pending.payload))
        self._write(
            {
                "version": QUEUE_VERSION,
                "state": "pending",
                "delivery_id": pending.delivery_id,
                "filename": filename,
                "payload_protected": base64.urlsafe_b64encode(protected)
                .decode("ascii")
                .rstrip("="),
            },
        )

    def confirm(self, delivery_id: str) -> None:
        if not DELIVERY_ID.fullmatch(delivery_id):
            raise ValueError("CSV 전달 식별자가 올바르지 않습니다.")
        self._write(
            {
                "version": QUEUE_VERSION,
                "state": "confirmed",
                "delivery_id": delivery_id,
            },
        )

    def clear(self) -> None:
        self.path.unlink(missing_ok=True)
        self.path.with_suffix(".tmp").unlink(missing_ok=True)

    def _write(self, value: dict[str, object]) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        temporary = self.path.with_suffix(".tmp")
        try:
            temporary.write_text(
                json.dumps(value, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            os.replace(temporary, self.path)
        finally:
            temporary.unlink(missing_ok=True)

    @staticmethod
    def _validate_filename(filename: str) -> str:
        safe_name = Path(filename).name
        if (
            safe_name != filename
            or not safe_name.lower().endswith(".csv")
            or len(safe_name.encode("utf-8")) > MAX_CSV_FILENAME_BYTES
        ):
            raise ValueError("CSV 파일 이름이 올바르지 않습니다.")
        return safe_name

    @staticmethod
    def _validate_payload(payload: bytes | bytearray) -> None:
        if not payload or len(payload) > MAX_CSV_BYTES:
            raise ValueError("CSV 파일은 1MB 이하여야 합니다.")
        bytes(payload).decode("utf-8-sig")
