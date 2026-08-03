from __future__ import annotations

import hashlib
import socket
import uuid

from .config import ConfigStore
from .protocol import ACK_BYTES, Pairing, decode_ack, encode_request


def main() -> None:
    config = ConfigStore().load()
    pairing = Pairing(
        receiver_id=config.receiver_id,
        secret=config.secret,
        host="127.0.0.1",
        port=config.port,
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
    matches[0].unlink()
    print("authenticated receiver save and cleanup verified")


if __name__ == "__main__":
    main()
