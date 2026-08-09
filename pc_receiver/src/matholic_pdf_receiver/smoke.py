from __future__ import annotations

import hashlib
import socket
import tempfile
import threading
import uuid
from pathlib import Path

from .config import ConfigStore, SecretProtector
from .protocol import ACK_BYTES, Pairing, decode_ack, encode_request
from .server import ReceiverState, ThreadedReceiverServer


def run_authenticated_roundtrip(
    secret_protector: SecretProtector | None = None,
) -> None:
    with tempfile.TemporaryDirectory(prefix="matholic-receiver-smoke-") as root_value:
        root = Path(root_value)
        store = ConfigStore(
            path=root / "config.json",
            secret_protector=secret_protector,
        )
        config = store.load_or_create()
        config.receive_dir = root / "received"
        store.save(config)
        state = ReceiverState(config, store)
        server = ThreadedReceiverServer(("127.0.0.1", 0), state)
        config.port = int(server.server_address[1])
        store.save(config)
        server_thread = threading.Thread(target=server.serve_forever, daemon=True)
        server_thread.start()
        try:
            pairing = Pairing(
                receiver_id=config.receiver_id,
                secret=config.secret,
                host="127.0.0.1",
                port=int(server.server_address[1]),
                display_name=config.display_name,
            )
            marker = uuid.uuid4().hex
            filename = f"__receiver_smoke_{marker}.pdf"
            pdf = b"%PDF-1.4\nsynthetic receiver smoke test\n%%EOF\n"
            frame = encode_request(pairing, filename, pdf)
            with socket.create_connection((pairing.host, pairing.port), timeout=5) as connection:
                connection.sendall(frame)
                ack = bytearray()
                while len(ack) < ACK_BYTES:
                    chunk = connection.recv(ACK_BYTES - len(ack))
                    if not chunk:
                        raise RuntimeError("receiver closed before acknowledgement")
                    ack.extend(chunk)
            verified = decode_ack(pairing, bytes(ack))
            if not verified.accepted or verified.pdf_sha256 != hashlib.sha256(pdf).digest():
                raise RuntimeError("receiver acknowledgement mismatch")

            matches = list(config.receive_dir.glob(f"*{marker}*.pdf"))
            if len(matches) != 1 or matches[0].read_bytes() != pdf:
                raise RuntimeError("receiver did not save the expected smoke PDF")
        finally:
            server.shutdown()
            server.server_close()
            server_thread.join(timeout=5)


def main() -> None:
    run_authenticated_roundtrip()
    print("isolated authenticated receiver save and cleanup verified")


if __name__ == "__main__":
    main()
