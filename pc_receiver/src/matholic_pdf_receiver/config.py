from __future__ import annotations

import base64
import json
import os
import socket
from dataclasses import dataclass, field
from pathlib import Path

from .protocol import Pairing, RECEIVER_ID_BYTES, SECRET_BYTES

DEFAULT_PORT = 48129
DEFAULT_FOLDER_NAME = "Matholic QR Cards"
MAX_REPLAY_IDS = 2048


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
    def __init__(self, path: Path | None = None) -> None:
        self.path = path or default_config_dir() / "config.json"

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
        if payload.get("version") != 1:
            raise ValueError("지원하지 않는 PC 수신기 설정입니다.")
        config = ReceiverConfig(
            receiver_id=base64.urlsafe_b64decode(payload["receiver_id"] + "=="),
            secret=base64.urlsafe_b64decode(payload["secret"] + "=="),
            port=int(payload["port"]),
            display_name=str(payload["display_name"]),
            receive_dir=Path(payload["receive_dir"]),
            replay_ids=list(payload.get("replay_ids", [])),
        )
        config.pairing(host="127.0.0.1")
        return config

    def save(self, config: ReceiverConfig) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        config.receive_dir.mkdir(parents=True, exist_ok=True)
        payload = {
            "version": 1,
            "receiver_id": base64.urlsafe_b64encode(config.receiver_id)
            .decode("ascii")
            .rstrip("="),
            "secret": base64.urlsafe_b64encode(config.secret).decode("ascii").rstrip("="),
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
