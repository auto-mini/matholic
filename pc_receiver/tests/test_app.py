from unittest.mock import patch

import pytest

from matholic_pdf_receiver import app


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
