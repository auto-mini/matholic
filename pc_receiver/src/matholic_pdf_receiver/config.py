from __future__ import annotations

import base64
import ctypes
import json
import os
import socket
from ctypes import wintypes
from dataclasses import dataclass, field
from pathlib import Path
from typing import Protocol

from .protocol import Pairing, RECEIVER_ID_BYTES, SECRET_BYTES

DEFAULT_PORT = 48129
DEFAULT_FOLDER_NAME = "Matholic QR Cards"
MAX_REPLAY_IDS = 2048
CONFIG_VERSION = 2
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
    replay_ids: list[str] = field(default_factory=list)

    def pairing(self, host: str | None = None) -> Pairing:
        return Pairing(
            receiver_id=self.receiver_id,
            secret=self.secret,
            host=host or current_lan_ipv4(),
            port=self.port,
            display_name=self.display_name,
        )

    def remember_request(self, request_id: bytes) -> None:
        encoded = request_id.hex()
        if encoded in self.replay_ids:
            raise ValueError("이미 처리한 전송 요청입니다.")
        self.replay_ids.append(encoded)
        if len(self.replay_ids) > MAX_REPLAY_IDS:
            del self.replay_ids[: len(self.replay_ids) - MAX_REPLAY_IDS]


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
        if version not in {1, CONFIG_VERSION}:
            raise ValueError("지원하지 않는 PC 수신기 설정입니다.")
        if version == 1:
            secret = base64.urlsafe_b64decode(payload["secret"] + "==")
        else:
            protected = base64.urlsafe_b64decode(payload["secret_protected"] + "==")
            secret = self.secret_protector.unprotect(protected)
        config = ReceiverConfig(
            receiver_id=base64.urlsafe_b64decode(payload["receiver_id"] + "=="),
            secret=secret,
            port=int(payload["port"]),
            display_name=str(payload["display_name"]),
            receive_dir=Path(payload["receive_dir"]),
            replay_ids=list(payload.get("replay_ids", [])),
        )
        config.pairing(host="127.0.0.1")
        if version == 1:
            # Fail closed if the plaintext legacy secret cannot be replaced.
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
            "replay_ids": config.replay_ids,
        }
        temporary = self.path.with_suffix(".tmp")
        temporary.write_text(
            json.dumps(payload, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
        os.replace(temporary, self.path)
