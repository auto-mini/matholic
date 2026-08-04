from types import SimpleNamespace
from unittest.mock import Mock, patch

import pytest

from matholic_pdf_receiver import app


def _lan_refresh_application(
    *,
    host: str | None = None,
    pairing_qr_available: bool = False,
) -> app.ReceiverApplication:
    application = app.ReceiverApplication.__new__(app.ReceiverApplication)
    application._shutting_down = False
    application.config = SimpleNamespace(display_name="TEST-PC", port=48129)
    application.host = host
    application._pairing_qr_available = pairing_qr_available
    application.address_var = Mock()
    application.pairing_status_var = Mock()
    application.pairing_qr_label = Mock()
    application.status_var = Mock()
    application.root = Mock()
    application.tray = Mock()
    application.qr_photo = Mock()
    return application


def test_lan_address_refresh_waits_and_retries_when_network_is_unavailable() -> None:
    application = _lan_refresh_application()

    with patch(
        "matholic_pdf_receiver.app.current_lan_ipv4",
        side_effect=OSError("network unavailable"),
    ):
        application._refresh_lan_address()

    assert application.host is None
    assert not application._pairing_qr_available
    application.address_var.set.assert_called_once_with("TEST-PC · 로컬 네트워크 연결 대기")
    application.pairing_status_var.set.assert_called_once_with(
        "사설 LAN 연결을 기다리고 있습니다.\n"
        "연결되면 페어링 QR이 자동으로 표시됩니다.",
    )
    application.pairing_qr_label.configure.assert_called_once_with(image="")
    assert application.qr_photo is None
    application.tray.notify.assert_not_called()
    application.root.after.assert_called_once_with(
        app.LAN_ADDRESS_REFRESH_MS,
        application._refresh_lan_address,
    )


def test_lan_address_refresh_builds_qr_when_network_becomes_available() -> None:
    application = _lan_refresh_application()

    with (
        patch("matholic_pdf_receiver.app.current_lan_ipv4", return_value="192.168.10.20"),
        patch.object(application, "_render_pairing_qr") as render_pairing_qr,
    ):
        application._refresh_lan_address()

    assert application.host == "192.168.10.20"
    assert application._pairing_qr_available
    application.address_var.set.assert_called_once_with("TEST-PC · 192.168.10.20:48129")
    render_pairing_qr.assert_called_once_with("192.168.10.20")
    application.status_var.set.assert_not_called()
    application.tray.notify.assert_not_called()


def test_lan_address_refresh_rebuilds_qr_and_warns_after_address_change() -> None:
    application = _lan_refresh_application(
        host="192.168.10.20",
        pairing_qr_available=True,
    )

    with (
        patch("matholic_pdf_receiver.app.current_lan_ipv4", return_value="192.168.10.21"),
        patch.object(application, "_render_pairing_qr") as render_pairing_qr,
    ):
        application._refresh_lan_address()

    assert application.host == "192.168.10.21"
    assert application._pairing_qr_available
    application.address_var.set.assert_called_once_with("TEST-PC · 192.168.10.21:48129")
    render_pairing_qr.assert_called_once_with("192.168.10.21")
    application.status_var.set.assert_called_once_with(
        "PC 주소가 바뀌었습니다. A 태블릿에서 새 QR로 다시 페어링하세요.",
    )
    application.tray.notify.assert_called_once_with(
        "PC 주소가 바뀌었습니다. 새 페어링 QR을 확인하세요.",
        app.APP_TITLE,
    )


def test_lan_address_refresh_restores_same_qr_without_repair_warning() -> None:
    application = _lan_refresh_application(
        host="192.168.10.20",
        pairing_qr_available=False,
    )

    with (
        patch("matholic_pdf_receiver.app.current_lan_ipv4", return_value="192.168.10.20"),
        patch.object(application, "_render_pairing_qr") as render_pairing_qr,
    ):
        application._refresh_lan_address()

    assert application._pairing_qr_available
    render_pairing_qr.assert_called_once_with("192.168.10.20")
    application.status_var.set.assert_not_called()
    application.tray.notify.assert_not_called()


def test_initialize_starts_server_before_lan_address_is_resolved(tmp_path) -> None:
    config = SimpleNamespace(
        display_name="TEST-PC",
        port=48129,
        receive_dir=tmp_path,
    )
    store = Mock()
    store.load_or_create.return_value = config
    receiver_state = Mock(pending_csv_name=None)
    root = Mock()
    tray = Mock()
    server = Mock()
    server_thread = Mock()
    tray_thread = Mock()

    def build_window(application: app.ReceiverApplication) -> None:
        application.pairing_qr_label = Mock()

    with (
        patch("matholic_pdf_receiver.app.ConfigStore", return_value=store),
        patch("matholic_pdf_receiver.app.ReceiverState", return_value=receiver_state),
        patch("matholic_pdf_receiver.app.tk.Tk", return_value=root),
        patch("matholic_pdf_receiver.app.tk.StringVar", side_effect=lambda **_: Mock()),
        patch.object(app.ReceiverApplication, "_build_window", build_window),
        patch("matholic_pdf_receiver.app._tray_image", return_value=Mock()),
        patch("matholic_pdf_receiver.app.pystray.Menu", return_value=Mock()),
        patch("matholic_pdf_receiver.app.pystray.MenuItem", return_value=Mock()),
        patch("matholic_pdf_receiver.app.pystray.Icon", return_value=tray),
        patch(
            "matholic_pdf_receiver.app.ThreadedReceiverServer",
            return_value=server,
        ) as server_type,
        patch(
            "matholic_pdf_receiver.app.threading.Thread",
            side_effect=[server_thread, tray_thread],
        ),
        patch("matholic_pdf_receiver.app.current_lan_ipv4") as lan_resolver,
    ):
        application = app.ReceiverApplication(show_window=False)

    lan_resolver.assert_not_called()
    server_type.assert_called_once_with(("0.0.0.0", 48129), receiver_state)
    server_thread.start.assert_called_once_with()
    tray_thread.start.assert_called_once_with()
    root.withdraw.assert_called_once_with()
    root.after.assert_any_call(0, application._refresh_lan_address)


def test_packaged_smoke_option_runs_authenticated_roundtrip(monkeypatch) -> None:
    monkeypatch.setattr("sys.argv", ["MatholicPdfReceiver.exe", "--smoke-check"])

    with patch("matholic_pdf_receiver.smoke.main") as smoke_main:
        app.main()

    smoke_main.assert_called_once_with()


def test_main_always_shuts_down_after_mainloop_failure(monkeypatch) -> None:
    monkeypatch.setattr("sys.argv", ["MatholicPdfReceiver.exe"])

    with patch("matholic_pdf_receiver.app.ReceiverApplication") as application_type:
        application = application_type.return_value
        application.run.side_effect = RuntimeError("mainloop failed")
        with pytest.raises(RuntimeError, match="mainloop failed"):
            app.main()

    application.shutdown.assert_called_once_with()
