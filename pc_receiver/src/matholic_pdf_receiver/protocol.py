from __future__ import annotations

import base64
import hashlib
import hmac
import os
import struct
import time
from dataclasses import dataclass

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.hkdf import HKDF

REQUEST_MAGIC = b"MATHPDF1"
ACK_MAGIC = b"MATHACK1"
PAIRING_PREFIX = "MATHOLIC-PC1:"
VERSION = 1
RECEIVER_ID_BYTES = 16
REQUEST_ID_BYTES = 16
NONCE_BYTES = 12
SECRET_BYTES = 32
SHA256_BYTES = 32
MAX_PDF_BYTES = 5 * 1024 * 1024
MAX_FILENAME_BYTES = 240
MAX_CIPHERTEXT_BYTES = MAX_PDF_BYTES + MAX_FILENAME_BYTES + 64
MAX_CLOCK_SKEW_SECONDS = 300

_REQUEST_FIXED = struct.Struct(">8sB16s16sq12sI")
_ACK_FIXED = struct.Struct(">8sB16s32s32s")
_PLAINTEXT_FIXED = struct.Struct(">HI")


class ProtocolError(ValueError):
    pass


@dataclass(frozen=True)
class Pairing:
    receiver_id: bytes
    secret: bytes
    host: str
    port: int
    display_name: str

    def __post_init__(self) -> None:
        if len(self.receiver_id) != RECEIVER_ID_BYTES:
            raise ProtocolError("receiver_id must be 16 bytes")
        if len(self.secret) != SECRET_BYTES:
            raise ProtocolError("secret must be 32 bytes")
        if not self.host or len(self.host) > 255:
            raise ProtocolError("host is invalid")
        if not 1024 <= self.port <= 65535:
            raise ProtocolError("port is invalid")
        if not self.display_name or len(self.display_name) > 80:
            raise ProtocolError("display_name is invalid")


@dataclass(frozen=True)
class DecodedRequest:
    request_id: bytes
    timestamp: int
    filename: str
    pdf: bytes


@dataclass(frozen=True)
class DecodedAck:
    accepted: bool
    request_id: bytes
    pdf_sha256: bytes


def _b64url_encode(value: bytes) -> str:
    return base64.urlsafe_b64encode(value).decode("ascii").rstrip("=")


def _b64url_decode(value: str) -> bytes:
    if not value or any(character.isspace() for character in value):
        raise ProtocolError("base64url value is invalid")
    padding = "=" * (-len(value) % 4)
    try:
        return base64.b64decode(value + padding, altchars=b"-_", validate=True)
    except ValueError as error:
        raise ProtocolError("base64url value is invalid") from error


def encode_pairing(pairing: Pairing) -> str:
    try:
        host = pairing.host.encode("ascii")
        name = pairing.display_name.encode("utf-8")
    except UnicodeEncodeError as error:
        raise ProtocolError("pairing host is invalid") from error
    if not 1 <= len(host) <= 255 or not 1 <= len(name) <= 255:
        raise ProtocolError("pairing text length is invalid")
    payload = (
        struct.pack(
            ">B16s32sHB",
            VERSION,
            pairing.receiver_id,
            pairing.secret,
            pairing.port,
            len(host),
        )
        + host
        + struct.pack(">B", len(name))
        + name
    )
    return PAIRING_PREFIX + _b64url_encode(payload)


def decode_pairing(value: str) -> Pairing:
    if not value.startswith(PAIRING_PREFIX):
        raise ProtocolError("pairing prefix is invalid")
    raw = _b64url_decode(value[len(PAIRING_PREFIX) :])
    fixed = struct.Struct(">B16s32sHB")
    if len(raw) < fixed.size + 2:
        raise ProtocolError("pairing payload is truncated")
    version, receiver_id, secret, port, host_length = fixed.unpack(raw[: fixed.size])
    if version != VERSION:
        raise ProtocolError("pairing version is invalid")
    host_start = fixed.size
    name_length_position = host_start + host_length
    if host_length == 0 or name_length_position >= len(raw):
        raise ProtocolError("pairing host length is invalid")
    name_length = raw[name_length_position]
    name_start = name_length_position + 1
    if name_length == 0 or name_start + name_length != len(raw):
        raise ProtocolError("pairing name length is invalid")
    return Pairing(
        receiver_id=receiver_id,
        secret=secret,
        host=raw[host_start:name_length_position].decode("ascii"),
        port=port,
        display_name=raw[name_start:].decode("utf-8"),
    )


def derive_transfer_key(secret: bytes, receiver_id: bytes) -> bytes:
    if len(secret) != SECRET_BYTES or len(receiver_id) != RECEIVER_ID_BYTES:
        raise ProtocolError("key material has an invalid length")
    return HKDF(
        algorithm=hashes.SHA256(),
        length=32,
        salt=receiver_id,
        info=b"matholic-pdf-transfer-v1",
    ).derive(secret)


def encode_request(
    pairing: Pairing,
    filename: str,
    pdf: bytes,
    *,
    timestamp: int | None = None,
    request_id: bytes | None = None,
    nonce: bytes | None = None,
) -> bytes:
    filename_bytes = filename.encode("utf-8")
    if not filename_bytes or len(filename_bytes) > MAX_FILENAME_BYTES:
        raise ProtocolError("filename is invalid")
    if not pdf.startswith(b"%PDF-") or len(pdf) > MAX_PDF_BYTES:
        raise ProtocolError("PDF is invalid")
    request_id = request_id or os.urandom(REQUEST_ID_BYTES)
    nonce = nonce or os.urandom(NONCE_BYTES)
    timestamp = int(time.time()) if timestamp is None else timestamp
    if len(request_id) != REQUEST_ID_BYTES or len(nonce) != NONCE_BYTES:
        raise ProtocolError("request randomness has an invalid length")
    plaintext = _PLAINTEXT_FIXED.pack(len(filename_bytes), len(pdf)) + filename_bytes + pdf
    header_without_length = struct.pack(
        ">8sB16s16sq12s",
        REQUEST_MAGIC,
        VERSION,
        pairing.receiver_id,
        request_id,
        timestamp,
        nonce,
    )
    ciphertext = AESGCM(derive_transfer_key(pairing.secret, pairing.receiver_id)).encrypt(
        nonce,
        plaintext,
        header_without_length,
    )
    return _REQUEST_FIXED.pack(
        REQUEST_MAGIC,
        VERSION,
        pairing.receiver_id,
        request_id,
        timestamp,
        nonce,
        len(ciphertext),
    ) + ciphertext


def request_frame_length(prefix: bytes) -> int:
    if len(prefix) != _REQUEST_FIXED.size:
        raise ProtocolError("request header length is invalid")
    magic, version, _, _, _, _, ciphertext_length = _REQUEST_FIXED.unpack(prefix)
    if magic != REQUEST_MAGIC or version != VERSION:
        raise ProtocolError("request header is invalid")
    if not 16 <= ciphertext_length <= MAX_CIPHERTEXT_BYTES:
        raise ProtocolError("request body length is invalid")
    return ciphertext_length


def decode_request(
    pairing: Pairing,
    frame: bytes,
    *,
    now: int | None = None,
) -> DecodedRequest:
    if len(frame) < _REQUEST_FIXED.size:
        raise ProtocolError("request is truncated")
    header = frame[: _REQUEST_FIXED.size]
    magic, version, receiver_id, request_id, timestamp, nonce, ciphertext_length = (
        _REQUEST_FIXED.unpack(header)
    )
    if magic != REQUEST_MAGIC or version != VERSION or receiver_id != pairing.receiver_id:
        raise ProtocolError("request target is invalid")
    if ciphertext_length != len(frame) - _REQUEST_FIXED.size:
        raise ProtocolError("request body length does not match")
    current_time = int(time.time()) if now is None else now
    if abs(current_time - timestamp) > MAX_CLOCK_SKEW_SECONDS:
        raise ProtocolError("request timestamp is outside the allowed window")
    header_without_length = struct.pack(
        ">8sB16s16sq12s",
        magic,
        version,
        receiver_id,
        request_id,
        timestamp,
        nonce,
    )
    try:
        plaintext = AESGCM(derive_transfer_key(pairing.secret, pairing.receiver_id)).decrypt(
            nonce,
            frame[_REQUEST_FIXED.size :],
            header_without_length,
        )
    except Exception as error:
        raise ProtocolError("request authentication failed") from error
    if len(plaintext) < _PLAINTEXT_FIXED.size:
        raise ProtocolError("request plaintext is truncated")
    filename_length, pdf_length = _PLAINTEXT_FIXED.unpack(
        plaintext[: _PLAINTEXT_FIXED.size],
    )
    expected_length = _PLAINTEXT_FIXED.size + filename_length + pdf_length
    if (
        not 1 <= filename_length <= MAX_FILENAME_BYTES
        or not 1 <= pdf_length <= MAX_PDF_BYTES
        or len(plaintext) != expected_length
    ):
        raise ProtocolError("request plaintext lengths are invalid")
    filename_start = _PLAINTEXT_FIXED.size
    try:
        filename = plaintext[filename_start : filename_start + filename_length].decode("utf-8")
    except UnicodeDecodeError as error:
        raise ProtocolError("filename encoding is invalid") from error
    pdf = plaintext[filename_start + filename_length :]
    if not pdf.startswith(b"%PDF-"):
        raise ProtocolError("payload is not a PDF")
    return DecodedRequest(
        request_id=request_id,
        timestamp=timestamp,
        filename=filename,
        pdf=pdf,
    )


def encode_ack(
    pairing: Pairing,
    request_id: bytes,
    pdf_sha256: bytes,
    *,
    accepted: bool,
) -> bytes:
    if len(request_id) != REQUEST_ID_BYTES or len(pdf_sha256) != SHA256_BYTES:
        raise ProtocolError("acknowledgement data is invalid")
    status = 1 if accepted else 0
    content = struct.pack(
        ">8sB16s32s",
        ACK_MAGIC,
        status,
        request_id,
        pdf_sha256,
    )
    signature = hmac.new(
        derive_transfer_key(pairing.secret, pairing.receiver_id),
        content,
        hashlib.sha256,
    ).digest()
    return _ACK_FIXED.pack(
        ACK_MAGIC,
        status,
        request_id,
        pdf_sha256,
        signature,
    )


def decode_ack(pairing: Pairing, frame: bytes) -> DecodedAck:
    if len(frame) != _ACK_FIXED.size:
        raise ProtocolError("acknowledgement length is invalid")
    magic, status, request_id, pdf_sha256, signature = _ACK_FIXED.unpack(frame)
    if magic != ACK_MAGIC or status not in (0, 1):
        raise ProtocolError("acknowledgement header is invalid")
    content = struct.pack(
        ">8sB16s32s",
        magic,
        status,
        request_id,
        pdf_sha256,
    )
    expected = hmac.new(
        derive_transfer_key(pairing.secret, pairing.receiver_id),
        content,
        hashlib.sha256,
    ).digest()
    if not hmac.compare_digest(signature, expected):
        raise ProtocolError("acknowledgement authentication failed")
    return DecodedAck(
        accepted=status == 1,
        request_id=request_id,
        pdf_sha256=pdf_sha256,
    )


REQUEST_HEADER_BYTES = _REQUEST_FIXED.size
ACK_BYTES = _ACK_FIXED.size
