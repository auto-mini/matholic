import socket
import threading
import time
from pathlib import Path

import pytest

from matholic_pdf_receiver import server as server_module
from matholic_pdf_receiver.config import ConfigStore, ReceiverConfig
from matholic_pdf_receiver.protocol import (
    CONTROL_CONFIRM_CSV,
    CONTROL_FETCH_CSV,
    CONTROL_STATUS,
    Pairing,
    decode_ack,
    decode_control_request,
    decode_control_response,
    encode_control_request,
    encode_request,
)
from matholic_pdf_receiver.server import ReceiverState, ThreadedReceiverServer, safe_pdf_name


class _TestSecretProtector:
    def protect(self, plaintext: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in plaintext)

    def unprotect(self, protected: bytes) -> bytes:
        return bytes(value ^ 0xA5 for value in protected)


def config_store(path: Path) -> ConfigStore:
    return ConfigStore(path, secret_protector=_TestSecretProtector())


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


def test_receiver_repeats_ack_without_duplicate_after_ack_loss(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = config_store(tmp_path / "config.json")
    store.save(config)
    pairing = config.pairing(host="127.0.0.1")
    frame = encode_request(pairing, "홍길동.pdf", b"%PDF-1.4\n%%EOF\n")
    state = ReceiverState(config, store)

    ack, destination = state.accept(frame)
    assert decode_ack(pairing, ack).accepted
    assert destination.read_bytes() == b"%PDF-1.4\n%%EOF\n"
    repeated_ack, repeated_destination = state.accept(frame)
    assert decode_ack(pairing, repeated_ack).accepted
    assert repeated_destination == destination
    assert list(config.receive_dir.glob("*.pdf")) == [destination]

    restarted = ReceiverState(store.load(), store)
    restarted_ack, restarted_destination = restarted.accept(frame)
    assert decode_ack(pairing, restarted_ack).accepted
    assert restarted_destination == destination
    assert list(config.receive_dir.glob("*.pdf")) == [destination]


def test_tcp_receiver_returns_authenticated_ack(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = config_store(tmp_path / "config.json")
    store.save(config)
    events = []
    state = ReceiverState(config, store, events.append)
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
        assert len(events) == 1
        assert events[0].message.endswith("student.pdf 저장 완료")
        assert events[0].notification_message == "카드 PDF 저장 완료"
        assert "student" not in events[0].notification_message
    finally:
        server.shutdown()
        server.server_close()
        thread.join(timeout=2)


def test_server_close_disconnects_partial_active_handler_before_returning(
    tmp_path: Path,
    config: ReceiverConfig,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(server_module, "MAX_ACTIVE_CONNECTIONS", 1)
    store = config_store(tmp_path / "config.json")
    store.save(config)
    state = ReceiverState(config, store)
    server = ThreadedReceiverServer(("127.0.0.1", 0), state)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    connection = socket.create_connection(server.server_address, timeout=2)
    try:
        connection.sendall(b"M")
        deadline = time.monotonic() + 2
        while time.monotonic() < deadline:
            if not server._connection_slots.acquire(blocking=False):
                break
            server._connection_slots.release()
            time.sleep(0.01)
        else:
            pytest.fail("partial request handler did not start")

        server.shutdown()
        thread.join(timeout=2)
        assert not thread.is_alive()
        server.server_close()

        connection.settimeout(0.25)
        try:
            assert connection.recv(1) == b""
        except (ConnectionAbortedError, ConnectionResetError):
            pass
        assert server._connection_slots.acquire(blocking=False)
        server._connection_slots.release()
    finally:
        connection.close()
        server.shutdown()
        server.server_close()
        thread.join(timeout=2)


def test_server_close_has_bounded_wait_for_handler_inside_state_operation(
    tmp_path: Path,
    config: ReceiverConfig,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(server_module, "HANDLER_SHUTDOWN_TIMEOUT_SECONDS", 0.05)
    store = config_store(tmp_path / "config.json")
    store.save(config)
    state = ReceiverState(config, store)
    pairing = config.pairing(host="127.0.0.1")
    frame = encode_request(pairing, "student.pdf", b"%PDF-1.4\n%%EOF\n")
    handler_started = threading.Event()
    release_handler = threading.Event()
    original_accept = state.accept

    def wait_inside_accept(payload: bytes) -> tuple[bytes, Path]:
        handler_started.set()
        assert release_handler.wait(timeout=2)
        return original_accept(payload)

    monkeypatch.setattr(state, "accept", wait_inside_accept)
    server = ThreadedReceiverServer(("127.0.0.1", 0), state)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    connection = socket.create_connection(server.server_address, timeout=2)
    try:
        connection.sendall(frame)
        assert handler_started.wait(timeout=2)
        server.shutdown()
        thread.join(timeout=2)
        assert not thread.is_alive()

        started = time.monotonic()
        server.server_close()
        elapsed = time.monotonic() - started

        assert elapsed < 0.5
        assert not server.active_handlers_drained
    finally:
        release_handler.set()
        connection.close()
        deadline = time.monotonic() + 2
        while time.monotonic() < deadline:
            if server._connection_slots.acquire(blocking=False):
                server._connection_slots.release()
                break
            time.sleep(0.01)
        server.shutdown()
        server.server_close()
        thread.join(timeout=2)


def test_receiver_accepts_status_and_serves_csv_once(
    tmp_path: Path,
    config: ReceiverConfig,
) -> None:
    store = config_store(tmp_path / "config.json")
    store.save(config)
    events = []
    state = ReceiverState(config, store, events.append)
    pairing = config.pairing(host="127.0.0.1")
    state.queue_csv("students.csv", "이름,아이디\n테스트,test".encode())

    status_frame = encode_control_request(
        pairing,
        CONTROL_STATUS,
        "ACTIVE",
        '{"state":"문제풀이","studentName":"테스트","notify":true}'.encode(),
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
    assert event.message == "테스트 · 문제풀이"
    assert event.notification_message == "문제풀이"
    assert event.student_name not in event.notification_message

    csv_frame = encode_control_request(pairing, CONTROL_FETCH_CSV, "FETCH")
    csv_request = decode_control_request(pairing, csv_frame)
    csv_response, csv_event = state.accept_control(csv_frame)
    decoded_csv = decode_control_response(
        pairing,
        csv_response,
        expected_request_id=csv_request.request_id,
    )
    assert decoded_csv.accepted
    delivery_id, filename = decoded_csv.label.split("|", 1)
    assert len(delivery_id) == 32
    assert filename == "students.csv"
    assert decoded_csv.payload.decode().startswith("이름")
    assert csv_event.kind == "csv_sent"

    second_frame = encode_control_request(pairing, CONTROL_FETCH_CSV, "FETCH")
    second_request = decode_control_request(pairing, second_frame)
    second_response, second_event = state.accept_control(second_frame)
    repeated_csv = decode_control_response(
        pairing,
        second_response,
        expected_request_id=second_request.request_id,
    )
    assert repeated_csv.accepted
    assert repeated_csv.label == decoded_csv.label
    assert repeated_csv.payload == decoded_csv.payload
    assert second_event.kind == "csv_sent"

    restarted = ReceiverState(store.load(), store)
    assert restarted.pending_csv_name == "students.csv"
    confirm_frame = encode_control_request(
        pairing,
        CONTROL_CONFIRM_CSV,
        delivery_id,
    )
    confirm_request = decode_control_request(pairing, confirm_frame)
    confirm_response, confirm_event = restarted.accept_control(confirm_frame)
    assert decode_control_response(
        pairing,
        confirm_response,
        expected_request_id=confirm_request.request_id,
    ).accepted
    assert confirm_event.kind == "csv_confirmed"
    assert restarted.pending_csv_name is None

    repeated_confirm_frame = encode_control_request(
        pairing,
        CONTROL_CONFIRM_CSV,
        delivery_id,
    )
    repeated_confirm_request = decode_control_request(pairing, repeated_confirm_frame)
    repeated_confirm_response, _ = restarted.accept_control(repeated_confirm_frame)
    assert decode_control_response(
        pairing,
        repeated_confirm_response,
        expected_request_id=repeated_confirm_request.request_id,
    ).accepted

    empty_frame = encode_control_request(pairing, CONTROL_FETCH_CSV, "FETCH")
    empty_request = decode_control_request(pairing, empty_frame)
    empty_response, _ = restarted.accept_control(empty_frame)
    assert not decode_control_response(
        pairing,
        empty_response,
        expected_request_id=empty_request.request_id,
    ).accepted
