import base64
import json
import os
import time
from pathlib import Path

import pytest

from matholic_pdf_receiver.config import (
    CONFIG_VERSION,
    ConfigStore,
    DpapiSecretProtector,
    ReceiverConfig,
)


class _TestSecretProtector:
    def protect(self, plaintext: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in plaintext)

    def unprotect(self, protected: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in protected)


def receiver_config(tmp_path: Path) -> ReceiverConfig:
    return ReceiverConfig(
        receiver_id=bytes.fromhex("00112233445566778899aabbccddeeff"),
        secret=bytes(range(32)),
        port=48129,
        display_name="TEST-PC",
        receive_dir=tmp_path / "received",
    )


def test_current_config_stores_only_protected_pairing_secret(tmp_path: Path) -> None:
    path = tmp_path / "config.json"
    config = receiver_config(tmp_path)
    store = ConfigStore(path, secret_protector=_TestSecretProtector())

    store.save(config)

    payload = json.loads(path.read_text(encoding="utf-8"))
    assert payload["version"] == CONFIG_VERSION
    assert "secret" not in payload
    protected = base64.urlsafe_b64decode(payload["secret_protected"] + "==")
    assert protected != config.secret
    loaded = store.load()
    assert loaded.receiver_id == config.receiver_id
    assert loaded.secret == config.secret


def test_replay_requests_are_time_bounded_without_count_eviction(
    tmp_path: Path,
    monkeypatch,
) -> None:
    import matholic_pdf_receiver.config as config_module

    config = receiver_config(tmp_path)
    now = int(time.time())
    monkeypatch.setattr(config_module, "MAX_REPLAY_REQUESTS", 2)
    first = bytes.fromhex("00" * 16)
    second = bytes.fromhex("11" * 16)
    third = bytes.fromhex("22" * 16)

    config.remember_request(first, now + 300)
    config.remember_request(second, now + 300)
    with pytest.raises(ValueError, match="가득"):
        config.remember_request(third, now + 300)
    assert config.has_seen_request(first, now + 299)

    config.purge_expired_requests(now + 301)
    config.remember_request(third, now + 600)
    assert config.has_seen_request(third, now + 301)


def test_v1_plaintext_config_is_migrated_on_first_load(tmp_path: Path) -> None:
    path = tmp_path / "config.json"
    config = receiver_config(tmp_path)
    legacy_payload = {
        "version": 1,
        "receiver_id": base64.urlsafe_b64encode(config.receiver_id)
        .decode("ascii")
        .rstrip("="),
        "secret": base64.urlsafe_b64encode(config.secret).decode("ascii").rstrip("="),
        "port": config.port,
        "display_name": config.display_name,
        "receive_dir": str(config.receive_dir),
        "replay_ids": [],
    }
    path.write_text(json.dumps(legacy_payload), encoding="utf-8")
    store = ConfigStore(path, secret_protector=_TestSecretProtector())

    loaded = store.load()

    assert loaded.secret == config.secret
    migrated = json.loads(path.read_text(encoding="utf-8"))
    assert migrated["version"] == CONFIG_VERSION
    assert "secret" not in migrated
    assert "secret_protected" in migrated


@pytest.mark.skipif(os.name != "nt", reason="Windows DPAPI is required")
def test_windows_dpapi_round_trip_is_not_plaintext() -> None:
    protector = DpapiSecretProtector()
    secret = bytes(range(32))

    protected = protector.protect(secret)

    assert protected != secret
    assert protector.unprotect(protected) == secret
