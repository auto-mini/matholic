from __future__ import annotations

import hashlib
import json
import os
import re
import socket
import socketserver
import threading
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Callable

from .config import ConfigStore, ReceiverConfig
from .protocol import (
    CONTROL_FETCH_CSV,
    CONTROL_MAGIC,
    CONTROL_STATUS,
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

SOCKET_TIMEOUT_SECONDS = 10
SAFE_FILENAME = re.compile(r"[^0-9A-Za-z가-힣._ -]+")


def _read_exact(connection: socket.socket, length: int) -> bytes:
    chunks: list[bytes] = []
    remaining = length
    while remaining:
        chunk = connection.recv(remaining)
        if not chunk:
            raise ProtocolError("전송이 중간에 종료되었습니다.")
        chunks.append(chunk)
        remaining -= len(chunk)
    return b"".join(chunks)


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


@dataclass(frozen=True)
class ReceiveEvent:
    accepted: bool
    message: str
    path: Path | None = None
    kind: str = "transfer"
    state: str | None = None
    student_name: str | None = None
    notify: bool = False


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
        self.pending_csv: tuple[str, bytes] | None = None

    def accept(self, frame: bytes) -> tuple[bytes, Path]:
        request = decode_request(self.config.pairing(host="127.0.0.1"), frame)
        pdf_hash = hashlib.sha256(request.pdf).digest()
        filename = safe_pdf_name(request.filename)
        with self.lock:
            if request.request_id.hex() in self.config.replay_ids:
                raise ProtocolError("이미 처리한 전송 요청입니다.")
            destination = unique_destination(self.config.receive_dir, filename)
            temporary = destination.with_suffix(destination.suffix + ".part")
            try:
                temporary.write_bytes(request.pdf)
                os.replace(temporary, destination)
                self.config.remember_request(request.request_id)
                self.store.save(self.config)
            except Exception:
                temporary.unlink(missing_ok=True)
                destination.unlink(missing_ok=True)
                raise
        ack = encode_ack(
            self.config.pairing(host="127.0.0.1"),
            request.request_id,
            pdf_hash,
            accepted=True,
        )
        return ack, destination

    def queue_csv(self, filename: str, payload: bytes) -> None:
        safe_name = Path(filename).name
        if not safe_name.lower().endswith(".csv"):
            raise ValueError("CSV 파일만 선택할 수 있습니다.")
        if not payload or len(payload) > 1024 * 1024:
            raise ValueError("CSV 파일은 1MB 이하여야 합니다.")
        payload.decode("utf-8-sig")
        with self.lock:
            self.pending_csv = (safe_name, bytes(payload))

    def clear_csv(self) -> None:
        with self.lock:
            self.pending_csv = None

    def accept_control(self, frame: bytes) -> tuple[bytes, ReceiveEvent]:
        request = decode_control_request(self.config.pairing(host="127.0.0.1"), frame)
        pairing = self.config.pairing(host="127.0.0.1")
        with self.lock:
            if request.request_id.hex() in self.config.replay_ids:
                raise ProtocolError("이미 처리한 제어 요청입니다.")
            self.config.remember_request(request.request_id)
            self.store.save(self.config)
            if request.operation == CONTROL_STATUS:
                event, label = self._status_event(request.label, request.payload)
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
                filename, payload = queued
                response = encode_control_response(
                    pairing,
                    request.request_id,
                    request.operation,
                    accepted=True,
                    label=filename,
                    payload=payload,
                )
                self.pending_csv = None
                return response, ReceiveEvent(
                    accepted=True,
                    message=f"{filename} 암호화 전송 완료",
                    kind="csv",
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
            ),
            label[:160],
        )


class _ReceiverHandler(socketserver.BaseRequestHandler):
    def handle(self) -> None:
        state: ReceiverState = self.server.receiver_state  # type: ignore[attr-defined]
        self.request.settimeout(SOCKET_TIMEOUT_SECONDS)
        try:
            magic = _read_exact(self.request, 8)
            header = magic + _read_exact(self.request, REQUEST_HEADER_BYTES - 8)
            if magic == REQUEST_MAGIC:
                body_length = request_frame_length(header)
            elif magic == CONTROL_MAGIC:
                body_length = secure_frame_body_length(header, CONTROL_MAGIC)
            else:
                raise ProtocolError("지원하지 않는 요청입니다.")
            frame = header + _read_exact(self.request, body_length)
            if magic == REQUEST_MAGIC:
                ack, destination = state.accept(frame)
                self.request.sendall(ack)
                state.on_event(
                    ReceiveEvent(
                        accepted=True,
                        message=f"{destination.name} 저장 완료",
                        path=destination,
                        notify=True,
                    ),
                )
            else:
                response, event = state.accept_control(frame)
                self.request.sendall(response)
                state.on_event(event)
        except Exception as error:
            state.on_event(
                ReceiveEvent(
                    accepted=False,
                    message=f"전송 거부: {error}",
                ),
            )


class ThreadedReceiverServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True

    def __init__(self, address: tuple[str, int], receiver_state: ReceiverState) -> None:
        self.receiver_state = receiver_state
        super().__init__(address, _ReceiverHandler)
