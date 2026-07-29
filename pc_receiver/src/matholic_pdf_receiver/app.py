from __future__ import annotations

import argparse
import queue
import socket
import threading
import tkinter as tk
from pathlib import Path
from tkinter import messagebox, ttk

import pystray
import qrcode
from PIL import Image, ImageDraw, ImageTk

from .config import ConfigStore, current_lan_ipv4
from .protocol import encode_pairing
from .server import ReceiveEvent, ReceiverState, ThreadedReceiverServer

APP_TITLE = "매쓰홀릭 PDF 수신기"


def _tray_image() -> Image.Image:
    image = Image.new("RGB", (64, 64), "#102A43")
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((8, 8, 56, 56), radius=8, fill="#FFFFFF")
    draw.rectangle((18, 17, 46, 42), fill="#102A43")
    draw.rectangle((23, 22, 41, 37), fill="#FFFFFF")
    draw.polygon([(20, 45), (44, 45), (32, 55)], fill="#2F80ED")
    return image


class ReceiverApplication:
    def __init__(self, show_window: bool) -> None:
        self.store = ConfigStore()
        self.config = self.store.load_or_create()
        self.host = current_lan_ipv4()
        self.events: queue.Queue[ReceiveEvent] = queue.Queue()
        self.server = ThreadedReceiverServer(
            ("0.0.0.0", self.config.port),
            ReceiverState(self.config, self.store, self.events.put),
        )
        self.server_thread = threading.Thread(
            target=self.server.serve_forever,
            name="matholic-pdf-receiver",
            daemon=True,
        )
        self.server_thread.start()

        self.root = tk.Tk()
        self.root.title(APP_TITLE)
        self.root.geometry("620x760")
        self.root.minsize(560, 680)
        self.root.protocol("WM_DELETE_WINDOW", self.hide_window)
        self.qr_photo: ImageTk.PhotoImage | None = None
        self.status_var = tk.StringVar(value="수신 대기 중")
        self.address_var = tk.StringVar(
            value=f"{self.config.display_name} · {self.host}:{self.config.port}",
        )
        self._build_window()
        if not show_window:
            self.root.withdraw()

        self.tray = pystray.Icon(
            "MatholicPdfReceiver",
            _tray_image(),
            APP_TITLE,
            menu=pystray.Menu(
                pystray.MenuItem("상태·페어링 QR 열기", self._tray_show, default=True),
                pystray.MenuItem("수신 폴더 열기", self._tray_open_folder),
                pystray.MenuItem("종료", self._tray_quit),
            ),
        )
        threading.Thread(target=self.tray.run, name="matholic-tray", daemon=True).start()
        self.root.after(200, self._poll_events)

    def _build_window(self) -> None:
        frame = ttk.Frame(self.root, padding=24)
        frame.pack(fill=tk.BOTH, expand=True)

        ttk.Label(frame, text=APP_TITLE, font=("Malgun Gothic", 20, "bold")).pack(
            pady=(0, 6),
        )
        ttk.Label(frame, textvariable=self.address_var, font=("Malgun Gothic", 11)).pack()
        ttk.Label(
            frame,
            text=(
                "A 태블릿 관리자 화면에서 아래 QR을 한 번 촬영하세요.\n"
                "이 QR에는 이 PC 전용 암호키가 들어 있으므로 외부에 공유하지 마세요."
            ),
            justify=tk.CENTER,
            font=("Malgun Gothic", 11),
        ).pack(pady=(14, 12))

        pairing_text = encode_pairing(self.config.pairing(self.host))
        qr = qrcode.QRCode(
            version=None,
            error_correction=qrcode.constants.ERROR_CORRECT_M,
            box_size=7,
            border=4,
        )
        qr.add_data(pairing_text)
        qr.make(fit=True)
        qr_image = qr.make_image(fill_color="black", back_color="white").convert("RGB")
        qr_image.thumbnail((400, 400), Image.Resampling.NEAREST)
        self.qr_photo = ImageTk.PhotoImage(qr_image)
        ttk.Label(frame, image=self.qr_photo).pack(pady=8)

        ttk.Separator(frame).pack(fill=tk.X, pady=12)
        ttk.Label(
            frame,
            textvariable=self.status_var,
            font=("Malgun Gothic", 13, "bold"),
            foreground="#102A43",
        ).pack(pady=6)
        ttk.Label(
            frame,
            text=f"저장 폴더: {self.config.receive_dir}",
            justify=tk.CENTER,
            wraplength=540,
            font=("Malgun Gothic", 10),
        ).pack(pady=(0, 12))
        ttk.Button(frame, text="수신 폴더 열기", command=self.open_folder).pack(
            ipadx=18,
            ipady=5,
        )
        ttk.Label(
            frame,
            text=(
                "창을 닫아도 수신기는 알림 영역에서 계속 실행됩니다.\n"
                "페어링되지 않은 기기와 위조·재전송 요청은 저장하지 않습니다."
            ),
            justify=tk.CENTER,
            font=("Malgun Gothic", 9),
            foreground="#52606D",
        ).pack(side=tk.BOTTOM, pady=(16, 0))

    def run(self) -> None:
        self.root.mainloop()

    def show_window(self) -> None:
        self.root.deiconify()
        self.root.lift()
        self.root.focus_force()

    def hide_window(self) -> None:
        self.root.withdraw()

    def open_folder(self) -> None:
        self.config.receive_dir.mkdir(parents=True, exist_ok=True)
        import os

        os.startfile(self.config.receive_dir)  # type: ignore[attr-defined]

    def _poll_events(self) -> None:
        try:
            while True:
                event = self.events.get_nowait()
                self.status_var.set(event.message)
                if event.accepted:
                    self.root.after(0, self.show_window)
        except queue.Empty:
            pass
        self.root.after(200, self._poll_events)

    def _tray_show(self, _icon: pystray.Icon, _item: pystray.MenuItem) -> None:
        self.root.after(0, self.show_window)

    def _tray_open_folder(self, _icon: pystray.Icon, _item: pystray.MenuItem) -> None:
        self.root.after(0, self.open_folder)

    def _tray_quit(self, _icon: pystray.Icon, _item: pystray.MenuItem) -> None:
        self.root.after(0, self.shutdown)

    def shutdown(self) -> None:
        self.tray.stop()
        self.server.shutdown()
        self.server.server_close()
        self.root.destroy()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=APP_TITLE)
    parser.add_argument(
        "--background",
        action="store_true",
        help="Start minimized to the notification area",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    try:
        application = ReceiverApplication(show_window=not args.background)
    except (OSError, RuntimeError, ValueError) as error:
        root = tk.Tk()
        root.withdraw()
        messagebox.showerror(APP_TITLE, f"수신기를 시작하지 못했습니다.\n\n{error}")
        root.destroy()
        raise SystemExit(1) from error
    application.run()


if __name__ == "__main__":
    main()
