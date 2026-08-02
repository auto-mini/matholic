from __future__ import annotations

import base64
import ctypes
import json
import os
import socket
import time
from ctypes import wintypes
from dataclasses import dataclass, field
from pathlib import Path
from typing import Protocol

from .protocol import Pairing, RECEIVER_ID_BYTES, SECRET_BYTES

DEFAULT_PORT = 48129
DEFAULT_FOLDER_NAME = "Matholic QR Cards"
MAX_REPLAY_REQUESTS = 65_536
REPLAY_WINDOW_SECONDS = 300
CONFIG_VERSION = 3
_DPAPI_ENTROPY = b"MatholicPdfReceiver/config/v2/pairing-secret"
_CRYPTPROTECT_UI_FORBIDDEN = 0x1


class SecretProtector(Protocol):
    def protect(self, plaintext: bytes) -> bytes: ...

    def unprotect(self, protected: bytes) -> bytes: ...


class _DataBlob(ctypes.Structure):
    _fields_ = [
        ("cbData", wintypes.DWORD),
        ("pbData", ctypes.POINTER(ctypes.c_ubyte)),
    ]


class DpapiSecretProtector:
    """Protect pairing secrets for the current Windows user with DPAPI."""

    def __init__(self) -> None:
        if os.name != "nt":
            raise RuntimeError("PC 수신기 비밀 보호는 Windows DPAPI가 필요합니다.")
        self._crypt32 = ctypes.WinDLL("crypt32", use_last_error=True)
        self._kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)
        self._crypt32.CryptProtectData.argtypes = [
            ctypes.POINTER(_DataBlob),
            wintypes.LPCWSTR,
            ctypes.POINTER(_DataBlob),
            ctypes.c_void_p,
            ctypes.c_void_p,
            wintypes.DWORD,
            ctypes.POINTER(_DataBlob),
        ]
        self._crypt32.CryptProtectData.restype = wintypes.BOOL
        self._crypt32.CryptUnprotectData.argtypes = [
            ctypes.POINTER(_DataBlob),
            ctypes.POINTER(wintypes.LPWSTR),
            ctypes.POINTER(_DataBlob),
            ctypes.c_void_p,
            ctypes.c_void_p,
            wintypes.DWORD,
            ctypes.POINTER(_DataBlob),
        ]
        self._crypt32.CryptUnprotectData.restype = wintypes.BOOL
        self._kernel32.LocalFree.argtypes = [ctypes.c_void_p]
        self._kernel32.LocalFree.restype = ctypes.c_void_p

    @staticmethod
    def _input_blob(data: bytes) -> tuple[_DataBlob, ctypes.Array[ctypes.c_ubyte]]:
        if not data:
            raise ValueError("보호할 비밀이 비어 있습니다.")
        buffer = (ctypes.c_ubyte * len(data)).from_buffer_copy(data)
        return _DataBlob(len(data), buffer), buffer

    def _transform(self, data: bytes, *, protect: bool) -> bytes:
        input_blob, input_buffer = self._input_blob(data)
        entropy_blob, entropy_buffer = self._input_blob(_DPAPI_ENTROPY)
        output_blob = _DataBlob()
        if protect:
            succeeded = self._crypt32.CryptProtectData(
                ctypes.byref(input_blob),
                "Matholic PDF Receiver pairing secret",
                ctypes.byref(entropy_blob),
                None,
                None,
                _CRYPTPROTECT_UI_FORBIDDEN,
                ctypes.byref(output_blob),
            )
        else:
            succeeded = self._crypt32.CryptUnprotectData(
                ctypes.byref(input_blob),
                None,
                ctypes.byref(entropy_blob),
                None,
                None,
                _CRYPTPROTECT_UI_FORBIDDEN,
                ctypes.byref(output_blob),
            )
        # Keep the ctypes input buffers alive until the native call returns.
        _ = input_buffer, entropy_buffer
        if not succeeded:
            error = ctypes.get_last_error()
            raise OSError(error, ctypes.FormatError(error))
        try:
            return ctypes.string_at(output_blob.pbData, output_blob.cbData)
        finally:
            self._kernel32.LocalFree(output_blob.pbData)

    def protect(self, plaintext: bytes) -> bytes:
        return self._transform(plaintext, protect=True)

    def unprotect(self, protected: bytes) -> bytes:
        return self._transform(protected, protect=False)


def default_config_dir() -> Path:
    root = os.environ.get("LOCALAPPDATA")
    if not root:
        root = str(Path.home() / "AppData" / "Local")
    return Path(root) / "MatholicPdfReceiver"


def default_receive_dir() -> Path:
    return Path.home() / "Downloads" / DEFAULT_FOLDER_NAME


def current_lan_ipv4() -> str:
    probe = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        probe.connect(("192.0.2.1", 9))
        address = probe.getsockname()[0]
    finally:
        probe.close()
    if not address or address.startswith("127."):
        raise RuntimeError("사용 가능한 로컬 네트워크 주소를 찾지 못했습니다.")
    return address


@dataclass
class ReceiverConfig:
    receiver_id: bytes
    secret: bytes
    port: int = DEFAULT_PORT
    display_name: str = field(default_factory=socket.gethostname)
    receive_dir: Path = field(default_factory=default_receive_dir)
    replay_requests: dict[str, int] = field(default_factory=dict)
    pdf_receipts: dict[str, dict[str, str]] = field(default_factory=dict)

    def pairing(self, host: str | None = None) -> Pairing:
        return Pairing(
            receiver_id=self.receiver_id,
            secret=self.secret,
            host=host or current_lan_ipv4(),
            port=self.port,
            display_name=self.display_name,
        )

    def purge_expired_requests(self, now_seconds: int | None = None) -> None:
        now_seconds = int(time.time()) if now_seconds is None else now_seconds
        expired = [
            request_id
            for request_id, expires_at in self.replay_requests.items()
            if expires_at < now_seconds
        ]
        for request_id in expired:
            self.replay_requests.pop(request_id, None)
            self.pdf_receipts.pop(request_id, None)

    def has_seen_request(self, request_id: bytes, now_seconds: int | None = None) -> bool:
        self.purge_expired_requests(now_seconds)
        return request_id.hex() in self.replay_requests

    def remember_request(self, request_id: bytes, expires_at: int) -> None:
        encoded = request_id.hex()
        self.purge_expired_requests()
        if encoded in self.replay_requests:
            raise ValueError("이미 처리한 전송 요청입니다.")
        if len(self.replay_requests) >= MAX_REPLAY_REQUESTS:
            raise ValueError("재전송 방지 저장소가 가득 찼습니다.")
        self.replay_requests[encoded] = expires_at

    def forget_request(self, request_id: bytes) -> None:
        encoded = request_id.hex()
        self.replay_requests.pop(encoded, None)
        self.pdf_receipts.pop(encoded, None)


class ConfigStore:
    def __init__(
        self,
        path: Path | None = None,
        secret_protector: SecretProtector | None = None,
    ) -> None:
        self.path = path or default_config_dir() / "config.json"
        self.secret_protector = secret_protector or DpapiSecretProtector()

    def load_or_create(self) -> ReceiverConfig:
        if self.path.exists():
            return self.load()
        config = ReceiverConfig(
            receiver_id=os.urandom(RECEIVER_ID_BYTES),
            secret=os.urandom(SECRET_BYTES),
        )
        self.save(config)
        return config

    def load(self) -> ReceiverConfig:
        payload = json.loads(self.path.read_text(encoding="utf-8"))
        version = payload.get("version")
        if version not in {1, 2, CONFIG_VERSION}:
            raise ValueError("지원하지 않는 PC 수신기 설정입니다.")
        if version == 1:
            secret = base64.urlsafe_b64decode(payload["secret"] + "==")
        else:
            protected = base64.urlsafe_b64decode(payload["secret_protected"] + "==")
            secret = self.secret_protector.unprotect(protected)
        legacy_replay_ids = list(payload.get("replay_ids", []))
        replay_requests = (
            {
                str(request_id): int(expires_at)
                for request_id, expires_at in payload.get("replay_requests", {}).items()
            }
            if version == CONFIG_VERSION
            else {
                str(request_id): int(time.time()) + REPLAY_WINDOW_SECONDS
                for request_id in legacy_replay_ids
            }
        )
        config = ReceiverConfig(
            receiver_id=base64.urlsafe_b64decode(payload["receiver_id"] + "=="),
            secret=secret,
            port=int(payload["port"]),
            display_name=str(payload["display_name"]),
            receive_dir=Path(payload["receive_dir"]),
            replay_requests=replay_requests,
            pdf_receipts={
                str(request_id): {
                    "sha256": str(receipt["sha256"]),
                    "path": str(receipt["path"]),
                }
                for request_id, receipt in payload.get("pdf_receipts", {}).items()
                if isinstance(receipt, dict) and "sha256" in receipt and "path" in receipt
            },
        )
        config.purge_expired_requests()
        config.pairing(host="127.0.0.1")
        if version != CONFIG_VERSION:
            # Fail closed if the legacy secret/cache cannot be replaced.
            self.save(config)
        return config

    def save(self, config: ReceiverConfig) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        config.receive_dir.mkdir(parents=True, exist_ok=True)
        payload = {
            "version": CONFIG_VERSION,
            "receiver_id": base64.urlsafe_b64encode(config.receiver_id)
            .decode("ascii")
            .rstrip("="),
            "secret_protected": base64.urlsafe_b64encode(
                self.secret_protector.protect(config.secret),
            )
            .decode("ascii")
            .rstrip("="),
            "port": config.port,
            "display_name": config.display_name,
            "receive_dir": str(config.receive_dir),
            "replay_requests": config.replay_requests,
            "pdf_receipts": config.pdf_receipts,
        }
        temporary = self.path.with_suffix(".tmp")
        try:
            temporary.write_text(
                json.dumps(payload, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            os.replace(temporary, self.path)
        finally:
            temporary.unlink(missing_ok=True)
