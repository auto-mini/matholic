import json
from pathlib import Path

from matholic_pdf_receiver.pending_csv import PendingCsv, PendingCsvStore


class _TestSecretProtector:
    def protect(self, plaintext: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in plaintext)

    def unprotect(self, protected: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in protected)


def test_pending_csv_is_protected_atomic_and_confirmed_idempotently(tmp_path: Path) -> None:
    path = tmp_path / "pending-csv.json"
    store = PendingCsvStore(path, _TestSecretProtector())
    payload = bytearray("이름,아이디,비밀번호\n가상,test,secret".encode())
    pending = PendingCsv("1" * 32, "students.csv", payload)

    store.save_pending(pending)

    serialized = path.read_text(encoding="utf-8")
    assert "가상" not in serialized
    assert "secret" not in serialized
    assert not path.with_suffix(".tmp").exists()
    loaded = store.load()
    assert loaded.pending is not None
    assert loaded.pending.delivery_id == pending.delivery_id
    assert loaded.pending.filename == pending.filename
    assert loaded.pending.payload == payload

    store.confirm(pending.delivery_id)

    confirmed = store.load()
    assert confirmed.pending is None
    assert confirmed.confirmed_delivery_id == pending.delivery_id
    value = json.loads(path.read_text(encoding="utf-8"))
    assert "payload_protected" not in value
    loaded.pending.clear_sensitive_data()
    assert all(value == 0 for value in loaded.pending.payload)
