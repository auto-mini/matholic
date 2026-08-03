from __future__ import annotations

import hashlib
import json
import os
import re
import socket
import socketserver
import threading
import time
import uuid
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Callable

from .config import ConfigStore, ReceiverConfig
from .protocol import (
    CONTROL_CONFIRM_CSV,
    CONTROL_FETCH_CSV,
    CONTROL_MAGIC,
    CONTROL_STATUS,
    MAX_CLOCK_SKEW_SECONDS,
    REQUEST_HEADER_BYTES,
    REQUEST_MAGIC,
    ProtocolError,
    decode_control_request,
    decode_request,
    encode_control_response,
    encode_ack,
    request_frame_length,
    secure_frame_body_length,
)
from .pending_csv import PendingCsv, PendingCsvStore

SOCKET_TIMEOUT_SECONDS = 10
SOCKET_TOTAL_DEADLINE_SECONDS = 30
MAX_ACTIVE_CONNECTIONS = 32
SAFE_FILENAME = re.compile(r"[^0-9A-Za-z가-힣._ -]+")


def _read_exact(
    connection: socket.socket,
    length: int,
    deadline: float,
) -> bytearray:
    result = bytearray(length)
    view = memoryview(result)
    offset = 0
    remaining = length
    try:
        while remaining:
            deadline_remaining = deadline - time.monotonic()
            if deadline_remaining <= 0:
                raise ProtocolError("전송 전체 제한 시간을 초과했습니다.")
            connection.settimeout(min(SOCKET_TIMEOUT_SECONDS, deadline_remaining))
            received = connection.recv_into(view[offset:], remaining)
            if not received:
                raise ProtocolError("전송이 중간에 종료되었습니다.")
            offset += received
            remaining -= received
        return result
    except Exception:
        result[:] = b"\x00" * len(result)
        raise


def safe_pdf_name(value: str) -> str:
    stem = Path(value).name
    if stem.lower().endswith(".pdf"):
        stem = stem[:-4]
    stem = SAFE_FILENAME.sub("_", stem).strip(" ._")[:80]
    return (stem or "matholic-qr-card") + ".pdf"


def unique_destination(folder: Path, filename: str, now: datetime | None = None) -> Path:
    now = now or datetime.now()
    prefix = now.strftime("%Y%m%d-%H%M%S")
    candidate = folder / f"{prefix}_{filename}"
    sequence = 2
    while candidate.exists():
        candidate = folder / f"{prefix}_{sequence}_{filename}"
        sequence += 1
    return candidate


def request_destination(
    folder: Path,
    filename: str,
    request_id: bytes,
    timestamp: int,
) -> Path:
    prefix = datetime.fromtimestamp(timestamp).strftime("%Y%m%d-%H%M%S")
    return folder / f"{prefix}_{request_id.hex()[:16]}_{filename}"


@dataclass(frozen=True)
class ReceiveEvent:
    accepted: bool
    message: str
    path: Path | None = None
    kind: str = "transfer"
    state: str | None = None
    student_name: str | None = None
    notify: bool = False
    notification_message: str | None = None


class ReceiverState:
    def __init__(
        self,
        config: ReceiverConfig,
        store: ConfigStore,
        on_event: Callable[[ReceiveEvent], None] | None = None,
    ) -> None:
        self.config = config
        self.store = store
        self.on_event = on_event or (lambda _: None)
        self.lock = threading.Lock()
        self.pending_csv_store = PendingCsvStore(
            store.path.with_name("pending-csv.json"),
            store.secret_protector,
        )
        snapshot = self.pending_csv_store.load()
        self.pending_csv = snapshot.pending
        self.confirmed_csv_delivery_id = snapshot.confirmed_delivery_id

    def accept(self, frame: bytes) -> tuple[bytes, Path]:
        request = decode_request(self.config.pairing(host="127.0.0.1"), frame)
        pdf_hash = hashlib.sha256(request.pdf).digest()
        filename = safe_pdf_name(request.filename)
        request_key = request.request_id.hex()
        with self.lock:
            if self.config.has_seen_request(request.request_id):
                receipt = self.config.pdf_receipts.get(request_key)
                if (
                    receipt is None
                    or receipt.get("sha256") != pdf_hash.hex()
                    or not Path(receipt.get("path", "")).is_file()
                ):
                    raise ProtocolError("이미 처리한 전송 요청입니다.")
                destination = Path(receipt["path"])
                ack = encode_ack(
                    self.config.pairing(host="127.0.0.1"),
                    request.request_id,
                    pdf_hash,
                    accepted=True,
                )
                return ack, destination
            destination = request_destination(
                self.config.receive_dir,
                filename,
                request.request_id,
                request.timestamp,
            )
            temporary = destination.with_suffix(destination.suffix + ".part")
            created_destination = False
            try:
                if destination.exists():
                    if hashlib.sha256(destination.read_bytes()).digest() != pdf_hash:
                        raise ProtocolError("같은 전송 식별자의 저장 파일이 일치하지 않습니다.")
                else:
                    with temporary.open("wb") as output:
                        output.write(request.pdf)
                        output.flush()
                        os.fsync(output.fileno())
                    os.replace(temporary, destination)
                    created_destination = True
                self.config.remember_request(
                    request.request_id,
                    request.timestamp + MAX_CLOCK_SKEW_SECONDS,
                )
                self.config.pdf_receipts[request_key] = {
                    "sha256": pdf_hash.hex(),
                    "path": str(destination),
                }
                self.store.save(self.config)
            except Exception:
                self.config.forget_request(request.request_id)
                temporary.unlink(missing_ok=True)
                if created_destination:
                    destination.unlink(missing_ok=True)
                raise
        ack = encode_ack(
            self.config.pairing(host="127.0.0.1"),
            request.request_id,
            pdf_hash,
            accepted=True,
        )
        return ack, destination

    @property
    def pending_csv_name(self) -> str | None:
        with self.lock:
            return self.pending_csv.filename if self.pending_csv else None

    def queue_csv(self, filename: str, payload: bytes | bytearray) -> None:
        safe_name = Path(filename).name
        if not safe_name.lower().endswith(".csv"):
            raise ValueError("CSV 파일만 선택할 수 있습니다.")
        if not payload or len(payload) > 1024 * 1024:
            raise ValueError("CSV 파일은 1MB 이하여야 합니다.")
        bytes(payload).decode("utf-8-sig")
        queued = PendingCsv(uuid.uuid4().hex, safe_name, bytearray(payload))
        with self.lock:
            try:
                self.pending_csv_store.save_pending(queued)
            except Exception:
                queued.clear_sensitive_data()
                raise
            previous = self.pending_csv
            self.pending_csv = queued
            self.confirmed_csv_delivery_id = None
            if previous is not None:
                previous.clear_sensitive_data()

    def clear_csv(self) -> None:
        with self.lock:
            self.pending_csv_store.clear()
            if self.pending_csv is not None:
                self.pending_csv.clear_sensitive_data()
            self.pending_csv = None
            self.confirmed_csv_delivery_id = None

    def close(self) -> None:
        with self.lock:
            if self.pending_csv is not None:
                self.pending_csv.clear_sensitive_data()
            self.pending_csv = None
            self.confirmed_csv_delivery_id = None

    def accept_control(self, frame: bytes) -> tuple[bytes, ReceiveEvent]:
        request = decode_control_request(self.config.pairing(host="127.0.0.1"), frame)
        pairing = self.config.pairing(host="127.0.0.1")
        if request.operation not in {
            CONTROL_STATUS,
            CONTROL_FETCH_CSV,
            CONTROL_CONFIRM_CSV,
        }:
            raise ProtocolError("지원하지 않는 제어 요청입니다.")
        status_result = (
            self._status_event(request.label, request.payload)
            if request.operation == CONTROL_STATUS
            else None
        )
        with self.lock:
            if self.config.has_seen_request(request.request_id):
                raise ProtocolError("이미 처리한 제어 요청입니다.")
            self.config.remember_request(
                request.request_id,
                request.timestamp + MAX_CLOCK_SKEW_SECONDS,
            )
            try:
                self.store.save(self.config)
            except Exception:
                self.config.forget_request(request.request_id)
                raise
            if request.operation == CONTROL_STATUS:
                event, label = status_result  # type: ignore[misc]
                response = encode_control_response(
                    pairing,
                    request.request_id,
                    request.operation,
                    accepted=True,
                    label=label,
                )
                return response, event
            if request.operation == CONTROL_FETCH_CSV:
                queued = self.pending_csv
                if queued is None:
                    response = encode_control_response(
                        pairing,
                        request.request_id,
                        request.operation,
                        accepted=False,
                        label="NO_CSV",
                    )
                    return response, ReceiveEvent(
                        accepted=False,
                        message="태블릿이 CSV를 요청했지만 대기 파일이 없습니다.",
                        kind="csv",
                    )
                response = encode_control_response(
                    pairing,
                    request.request_id,
                    request.operation,
                    accepted=True,
                    label=f"{queued.delivery_id}|{queued.filename}",
                    payload=queued.payload,
                )
                return response, ReceiveEvent(
                    accepted=True,
                    message=f"{queued.filename} 전송 완료 · 태블릿 적용 확인 대기",
                    kind="csv_sent",
                )
            if request.operation == CONTROL_CONFIRM_CSV:
                delivery_id = request.label.strip()
                if self.pending_csv is not None and delivery_id == self.pending_csv.delivery_id:
                    self.pending_csv_store.confirm(delivery_id)
                    self.pending_csv.clear_sensitive_data()
                    self.pending_csv = None
                    self.confirmed_csv_delivery_id = delivery_id
                    accepted = True
                else:
                    accepted = delivery_id == self.confirmed_csv_delivery_id
                response = encode_control_response(
                    pairing,
                    request.request_id,
                    request.operation,
                    accepted=accepted,
                    label="CONFIRMED" if accepted else "CSV_DELIVERY_MISMATCH",
                )
                return response, ReceiveEvent(
                    accepted=accepted,
                    message=(
                        "학생 CSV 적용 확인 완료"
                        if accepted
                        else "학생 CSV 적용 확인 식별자가 일치하지 않습니다."
                    ),
                    kind="csv_confirmed",
                )
        raise ProtocolError("지원하지 않는 제어 요청입니다.")

    @staticmethod
    def _status_event(label: str, payload: bytes) -> tuple[ReceiveEvent, str]:
        if len(payload) > 4096:
            raise ProtocolError("상태 정보가 너무 큽니다.")
        try:
            value = json.loads(payload.decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError) as error:
            raise ProtocolError("상태 정보 형식이 올바르지 않습니다.") from error
        if not isinstance(value, dict):
            raise ProtocolError("상태 정보 형식이 올바르지 않습니다.")
        state = str(value.get("state", "")).strip()
        student_name = str(value.get("studentName", "")).strip() or None
        notify = bool(value.get("notify", False))
        if (
            not state
            or len(state) > 80
            or (student_name is not None and len(student_name) > 80)
        ):
            raise ProtocolError("상태 정보 값이 올바르지 않습니다.")
        message = state if student_name is None else f"{student_name} · {state}"
        return (
            ReceiveEvent(
                accepted=True,
                message=message,
                kind="status",
                state=state,
                student_name=student_name,
                notify=notify,
                notification_message=state if notify else None,
            ),
            label[:160],
        )


class _ReceiverHandler(socketserver.BaseRequestHandler):
    def handle(self) -> None:
        state: ReceiverState = self.server.receiver_state  # type: ignore[attr-defined]
        deadline = time.monotonic() + SOCKET_TOTAL_DEADLINE_SECONDS
        frame: bytearray | None = None
        response_frame: bytearray | None = None
        try:
            magic = _read_exact(self.request, 8, deadline)
            header = magic + _read_exact(
                self.request,
                REQUEST_HEADER_BYTES - 8,
                deadline,
            )
            if magic == REQUEST_MAGIC:
                body_length = request_frame_length(header)
            elif magic == CONTROL_MAGIC:
                body_length = secure_frame_body_length(header, CONTROL_MAGIC)
            else:
                raise ProtocolError("지원하지 않는 요청입니다.")
            frame = header + _read_exact(self.request, body_length, deadline)
            if magic == REQUEST_MAGIC:
                ack, destination = state.accept(frame)
                response_frame = bytearray(ack)
                self.request.sendall(response_frame)
                state.on_event(
                    ReceiveEvent(
                        accepted=True,
                        message=f"{destination.name} 저장 완료",
                        path=destination,
                        notify=True,
                        notification_message="카드 PDF 저장 완료",
                    ),
                )
            else:
                response, event = state.accept_control(frame)
                response_frame = bytearray(response)
                self.request.sendall(response_frame)
                state.on_event(event)
        except Exception as error:
            state.on_event(
                ReceiveEvent(
                    accepted=False,
                    message=f"전송 거부: {error}",
                ),
            )
        finally:
            if frame is not None:
                frame[:] = b"\x00" * len(frame)
            if response_frame is not None:
                response_frame[:] = b"\x00" * len(response_frame)


class ThreadedReceiverServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True
    request_queue_size = 16

    def __init__(self, address: tuple[str, int], receiver_state: ReceiverState) -> None:
        self.receiver_state = receiver_state
        self._connection_slots = threading.BoundedSemaphore(MAX_ACTIVE_CONNECTIONS)
        super().__init__(address, _ReceiverHandler)

    def process_request(self, request: socket.socket, client_address: tuple[str, int]) -> None:
        if not self._connection_slots.acquire(blocking=False):
            request.close()
            return
        try:
            super().process_request(request, client_address)
        except Exception:
            self._connection_slots.release()
            raise

    def process_request_thread(
        self,
        request: socket.socket,
        client_address: tuple[str, int],
    ) -> None:
        try:
            super().process_request_thread(request, client_address)
        finally:
            self._connection_slots.release()
