from __future__ import annotations

import hashlib
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
    REQUEST_HEADER_BYTES,
    ProtocolError,
    decode_request,
    encode_ack,
    request_frame_length,
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


class _ReceiverHandler(socketserver.BaseRequestHandler):
    def handle(self) -> None:
        state: ReceiverState = self.server.receiver_state  # type: ignore[attr-defined]
        self.request.settimeout(SOCKET_TIMEOUT_SECONDS)
        try:
            header = _read_exact(self.request, REQUEST_HEADER_BYTES)
            body_length = request_frame_length(header)
            frame = header + _read_exact(self.request, body_length)
            ack, destination = state.accept(frame)
            self.request.sendall(ack)
            state.on_event(
                ReceiveEvent(
                    accepted=True,
                    message=f"{destination.name} 저장 완료",
                    path=destination,
                ),
            )
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
