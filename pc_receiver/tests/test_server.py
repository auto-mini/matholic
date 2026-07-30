import socket
import threading
from pathlib import Path

import pytest

from matholic_pdf_receiver.config import ConfigStore, ReceiverConfig
from matholic_pdf_receiver.protocol import (
    CONTROL_FETCH_CSV,
    CONTROL_STATUS,
    Pairing,
    ProtocolError,
    decode_ack,
    decode_control_request,
    decode_control_response,
    encode_control_request,
    encode_request,
)
from matholic_pdf_receiver.server import ReceiverState, ThreadedReceiverServer, safe_pdf_name


@pytest.fixture
def config(tmp_path: Path) -> ReceiverConfig:
    return ReceiverConfig(
        receiver_id=bytes.fromhex("00112233445566778899aabbccddeeff"),
        secret=bytes(range(32)),
        port=48129,
        display_name="TEST-PC",
        receive_dir=tmp_path / "received",
    )


def test_filename_is_sanitized() -> None:
    assert safe_pdf_name("../../홍길동:QR?.pdf") == "홍길동_QR.pdf"


def test_receiver_saves_once_and_rejects_replay(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = ConfigStore(tmp_path / "config.json")
    store.save(config)
    pairing = config.pairing(host="127.0.0.1")
    frame = encode_request(pairing, "홍길동.pdf", b"%PDF-1.4\n%%EOF\n")
    state = ReceiverState(config, store)

    ack, destination = state.accept(frame)
    assert decode_ack(pairing, ack).accepted
    assert destination.read_bytes() == b"%PDF-1.4\n%%EOF\n"
    with pytest.raises(ProtocolError, match="이미 처리"):
        state.accept(frame)


def test_tcp_receiver_returns_authenticated_ack(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = ConfigStore(tmp_path / "config.json")
    store.save(config)
    state = ReceiverState(config, store)
    server = ThreadedReceiverServer(("127.0.0.1", 0), state)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        pairing = Pairing(
            receiver_id=config.receiver_id,
            secret=config.secret,
            host="127.0.0.1",
            port=server.server_address[1],
            display_name=config.display_name,
        )
        frame = encode_request(pairing, "student.pdf", b"%PDF-1.4\n%%EOF\n")
        with socket.create_connection(server.server_address, timeout=2) as connection:
            connection.sendall(frame)
            ack = connection.recv(89)
        assert decode_ack(pairing, ack).accepted
        assert list(config.receive_dir.glob("*.pdf"))
    finally:
        server.shutdown()
        server.server_close()
        thread.join(timeout=2)


def test_receiver_accepts_status_and_serves_csv_once(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = ConfigStore(tmp_path / "config.json")
    store.save(config)
    events = []
    state = ReceiverState(config, store, events.append)
    pairing = config.pairing(host="127.0.0.1")
    state.queue_csv("students.csv", "이름,아이디\n테스트,test".encode())

    status_frame = encode_control_request(
        pairing,
        CONTROL_STATUS,
        "ACTIVE",
        '{"state":"문제풀이","studentName":"테스트","notify":false}'.encode(),
    )
    status_response, event = state.accept_control(status_frame)
    decoded_status = decode_control_response(
        pairing,
        status_response,
        expected_request_id=decode_control_request(pairing, status_frame).request_id,
    )
    assert decoded_status.accepted
    assert event.kind == "status"
    assert event.student_name == "테스트"

    csv_frame = encode_control_request(pairing, CONTROL_FETCH_CSV, "FETCH")
    csv_request = decode_control_request(pairing, csv_frame)
    csv_response, csv_event = state.accept_control(csv_frame)
    decoded_csv = decode_control_response(
        pairing,
        csv_response,
        expected_request_id=csv_request.request_id,
    )
    assert decoded_csv.accepted
    assert decoded_csv.label == "students.csv"
    assert decoded_csv.payload.decode().startswith("이름")
    assert csv_event.kind == "csv"

    second_frame = encode_control_request(pairing, CONTROL_FETCH_CSV, "FETCH")
    second_request = decode_control_request(pairing, second_frame)
    second_response, _ = state.accept_control(second_frame)
    assert not decode_control_response(
        pairing,
        second_response,
        expected_request_id=second_request.request_id,
    ).accepted
