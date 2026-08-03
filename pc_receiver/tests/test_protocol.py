import hashlib

import pytest

from matholic_pdf_receiver.protocol import (
    ACK_BYTES,
    CONTROL_FETCH_CSV,
    CONTROL_STATUS,
    Pairing,
    ProtocolError,
    decode_ack,
    decode_control_request,
    decode_control_response,
    decode_pairing,
    decode_request,
    encode_ack,
    encode_control_request,
    encode_control_response,
    encode_pairing,
    encode_request,
)


PAIRING = Pairing(
    receiver_id=bytes.fromhex("00112233445566778899aabbccddeeff"),
    secret=bytes(range(32)),
    host="192.168.219.224",
    port=48129,
    display_name="MATHOLIC-PC",
)
REQUEST_ID = bytes.fromhex("102132435465768798a9babbdcddedef")
NONCE = bytes.fromhex("00112233445566778899aabb")
PDF = b"%PDF-1.4\nmatholic-test\n%%EOF\n"


def test_pairing_round_trip() -> None:
    encoded = encode_pairing(PAIRING)
    assert encoded == (
        "MATHOLIC-PC1:"
        "AQARIjNEVWZ3iJmqu8zd7v8AAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eH7wB"
        "DzE5Mi4xNjguMjE5LjIyNAtNQVRIT0xJQy1QQw"
    )
    assert decode_pairing(encoded) == PAIRING


def test_request_and_authenticated_ack_round_trip() -> None:
    frame = encode_request(
        PAIRING,
        "홍길동 QR.pdf",
        PDF,
        timestamp=1_800_000_000,
        request_id=REQUEST_ID,
        nonce=NONCE,
    )
    decoded = decode_request(PAIRING, frame, now=1_800_000_010)
    assert decoded.request_id == REQUEST_ID
    assert decoded.filename == "홍길동 QR.pdf"
    assert decoded.pdf == PDF

    ack = encode_ack(
        PAIRING,
        REQUEST_ID,
        hashlib.sha256(PDF).digest(),
        accepted=True,
    )
    assert len(ack) == ACK_BYTES
    decoded_ack = decode_ack(PAIRING, ack)
    assert decoded_ack.accepted
    assert decoded_ack.request_id == REQUEST_ID
    assert decoded_ack.pdf_sha256 == hashlib.sha256(PDF).digest()


def test_tampered_request_and_ack_are_rejected() -> None:
    frame = bytearray(
        encode_request(
            PAIRING,
            "student.pdf",
            PDF,
            timestamp=1_800_000_000,
            request_id=REQUEST_ID,
            nonce=NONCE,
        ),
    )
    frame[-1] ^= 1
    with pytest.raises(ProtocolError, match="authentication"):
        decode_request(PAIRING, bytes(frame), now=1_800_000_000)

    ack = bytearray(
        encode_ack(
            PAIRING,
            REQUEST_ID,
            hashlib.sha256(PDF).digest(),
            accepted=True,
        ),
    )
    ack[-1] ^= 1
    with pytest.raises(ProtocolError, match="authentication"):
        decode_ack(PAIRING, bytes(ack))


def test_stale_request_is_rejected() -> None:
    frame = encode_request(
        PAIRING,
        "student.pdf",
        PDF,
        timestamp=1_800_000_000,
        request_id=REQUEST_ID,
        nonce=NONCE,
    )
    with pytest.raises(ProtocolError, match="timestamp"):
        decode_request(PAIRING, frame, now=1_800_000_301)


def test_control_status_and_response_round_trip() -> None:
    frame = encode_control_request(
        PAIRING,
        CONTROL_STATUS,
        "ACTIVE",
        '{"state":"문제풀이","studentName":"테스트","notify":false}'.encode(),
        timestamp=1_800_000_000,
        request_id=REQUEST_ID,
        nonce=NONCE,
    )
    decoded = decode_control_request(PAIRING, frame, now=1_800_000_010)
    assert decoded.operation == CONTROL_STATUS
    assert decoded.label == "ACTIVE"
    assert decoded.request_id == REQUEST_ID
    assert b'"studentName"' in decoded.payload

    response = encode_control_response(
        PAIRING,
        REQUEST_ID,
        CONTROL_STATUS,
        accepted=True,
        label="ACTIVE",
        timestamp=1_800_000_010,
        nonce=bytes.fromhex("102132435465768798a9babb"),
    )
    decoded_response = decode_control_response(
        PAIRING,
        response,
        expected_request_id=REQUEST_ID,
        now=1_800_000_020,
    )
    assert decoded_response.accepted
    assert decoded_response.operation == CONTROL_STATUS
    assert decoded_response.label == "ACTIVE"


def test_control_csv_response_is_authenticated() -> None:
    response = bytearray(
        encode_control_response(
            PAIRING,
            REQUEST_ID,
            CONTROL_FETCH_CSV,
            accepted=True,
            label="students.csv",
            payload="이름,아이디\n테스트,test".encode(),
            timestamp=1_800_000_000,
            nonce=NONCE,
        ),
    )
    decoded = decode_control_response(
        PAIRING,
        bytes(response),
        expected_request_id=REQUEST_ID,
        now=1_800_000_000,
    )
    assert decoded.label == "students.csv"
    assert decoded.payload.decode().startswith("이름")
    response[-1] ^= 1
    with pytest.raises(ProtocolError, match="authentication"):
        decode_control_response(PAIRING, bytes(response), now=1_800_000_000)
