from unittest.mock import patch

from matholic_pdf_receiver import app


def test_packaged_smoke_option_runs_authenticated_roundtrip(monkeypatch) -> None:
    monkeypatch.setattr("sys.argv", ["MatholicPdfReceiver.exe", "--smoke-check"])

    with patch("matholic_pdf_receiver.smoke.main") as smoke_main:
        app.main()

    smoke_main.assert_called_once_with()
