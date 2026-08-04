from __future__ import annotations

import argparse
import queue
import socket
import threading
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

import pystray
import qrcode
from PIL import Image, ImageDraw, ImageTk

from matholic_pdf_receiver.config import ConfigStore, current_lan_ipv4
from matholic_pdf_receiver.protocol import encode_pairing
from matholic_pdf_receiver.server import ReceiveEvent, ReceiverState, ThreadedReceiverServer

APP_TITLE = "매쓰홀릭 PDF 수신기"
PAIRING_QR_PREVIEW_PX = 240
MAX_EVENT_QUEUE = 256
MAX_EVENTS_PER_POLL = 64
LAN_ADDRESS_REFRESH_MS = 5_000


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
        self._shutting_down = False
        self.server: ThreadedReceiverServer | None = None
        self.server_thread: threading.Thread | None = None
        self.tray: pystray.Icon | None = None
        self.tray_thread: threading.Thread | None = None
        self.root: tk.Tk | None = None
        self.receiver_state: ReceiverState | None = None
        try:
            self._initialize(show_window)
        except Exception:
            self._shutting_down = True
            self._cleanup_resources()
            raise

    def _initialize(self, show_window: bool) -> None:
        self.store = ConfigStore()
        self.config = self.store.load_or_create()
        self.host: str | None = None
        self._pairing_qr_available = False
        self.events: queue.Queue[ReceiveEvent] = queue.Queue(maxsize=MAX_EVENT_QUEUE)
        self.receiver_state = ReceiverState(self.config, self.store, self._enqueue_event)
        self.root = tk.Tk()
        self.root.title(APP_TITLE)
        self.root.geometry("620x760")
        self.root.minsize(560, 680)
        self.root.protocol("WM_DELETE_WINDOW", self.hide_window)
        self.qr_photo: ImageTk.PhotoImage | None = None
        self.status_var = tk.StringVar(value="수신 대기 중")
        self.kiosk_state_var = tk.StringVar(value="태블릿 상태: 연결 대기")
        self.student_var = tk.StringVar(value="학생: 없음")
        pending_csv_name = self.receiver_state.pending_csv_name
        self.csv_var = tk.StringVar(
            value=(
                f"학생 CSV: {pending_csv_name} · 태블릿 요청 대기"
                if pending_csv_name
                else "학생 CSV: 대기 파일 없음"
            ),
        )
        self.address_var = tk.StringVar(
            value=f"{self.config.display_name} · 로컬 네트워크 확인 중",
        )
        self.pairing_status_var = tk.StringVar(
            value="사설 LAN 주소를 확인하는 중입니다.",
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
        self.server = ThreadedReceiverServer(
            ("0.0.0.0", self.config.port),
            self.receiver_state,
        )
        self.server_thread = threading.Thread(
            target=self._run_server,
            name="matholic-pdf-receiver",
            daemon=True,
        )
        self.server_thread.start()
        self.tray_thread = threading.Thread(
            target=self._run_tray,
            name="matholic-tray",
            daemon=True,
        )
        self.tray_thread.start()
        self.root.after(200, self._poll_events)
        self.root.after(0, self._refresh_lan_address)

    def _enqueue_event(self, event: ReceiveEvent) -> None:
        try:
            self.events.put_nowait(event)
        except queue.Full:
            try:
                self.events.get_nowait()
            except queue.Empty:
                pass
            try:
                self.events.put_nowait(event)
            except queue.Full:
                pass

    def _run_server(self) -> None:
        try:
            assert self.server is not None
            self.server.serve_forever()
        except Exception as error:
            if not self._shutting_down:
                self._enqueue_event(
                    ReceiveEvent(False, f"수신 서버가 중지되었습니다: {error}", kind="fatal"),
                )

    def _run_tray(self) -> None:
        try:
            assert self.tray is not None
            self.tray.run()
        except Exception as error:
            if not self._shutting_down:
                self._enqueue_event(
                    ReceiveEvent(False, f"알림 영역 실행에 실패했습니다: {error}", kind="fatal"),
                )

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

        self.pairing_qr_label = ttk.Label(
            frame,
            textvariable=self.pairing_status_var,
            justify=tk.CENTER,
        )
        self.pairing_qr_label.pack(pady=8)

        ttk.Separator(frame).pack(fill=tk.X, pady=12)
        state_box = ttk.LabelFrame(frame, text="A 태블릿 실시간 상태", padding=12)
        state_box.pack(fill=tk.X, pady=(0, 12))
        ttk.Label(
            state_box,
            textvariable=self.kiosk_state_var,
            font=("Malgun Gothic", 13, "bold"),
            foreground="#102A43",
        ).pack(anchor=tk.W)
        ttk.Label(
            state_box,
            textvariable=self.student_var,
            font=("Malgun Gothic", 11),
        ).pack(anchor=tk.W, pady=(5, 0))

        ttk.Label(
            frame,
            text="학생 CSV 암호화 전송",
            font=("Malgun Gothic", 12, "bold"),
        ).pack()
        ttk.Label(
            frame,
            textvariable=self.csv_var,
            justify=tk.CENTER,
            wraplength=540,
            font=("Malgun Gothic", 10),
        ).pack(pady=(4, 8))
        csv_buttons = ttk.Frame(frame)
        csv_buttons.pack()
        ttk.Button(
            csv_buttons,
            text="CSV 선택",
            command=self.choose_csv,
        ).pack(side=tk.LEFT, padx=4, ipadx=12, ipady=4)
        ttk.Button(
            csv_buttons,
            text="대기 취소",
            command=self.clear_csv,
        ).pack(side=tk.LEFT, padx=4, ipadx=12, ipady=4)
        ttk.Separator(frame).pack(fill=tk.X, pady=14)
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

    def _render_pairing_qr(self, host: str) -> None:
        pairing_text = encode_pairing(self.config.pairing(host))
        qr = qrcode.QRCode(
            version=None,
            error_correction=qrcode.constants.ERROR_CORRECT_M,
            box_size=7,
            border=4,
        )
        qr.add_data(pairing_text)
        qr.make(fit=True)
        qr_image = qr.make_image(fill_color="black", back_color="white").convert("RGB")
        qr_image.thumbnail(
            (PAIRING_QR_PREVIEW_PX, PAIRING_QR_PREVIEW_PX),
            Image.Resampling.NEAREST,
        )
        self.qr_photo = ImageTk.PhotoImage(qr_image)
        self.pairing_status_var.set("")
        self.pairing_qr_label.configure(image=self.qr_photo)

    def _refresh_lan_address(self) -> None:
        if self._shutting_down:
            return
        try:
            host = current_lan_ipv4()
        except (OSError, RuntimeError, ValueError):
            self._pairing_qr_available = False
            self.address_var.set(
                f"{self.config.display_name} · 로컬 네트워크 연결 대기",
            )
            self.pairing_status_var.set(
                "사설 LAN 연결을 기다리고 있습니다.\n"
                "연결되면 페어링 QR이 자동으로 표시됩니다.",
            )
            self.pairing_qr_label.configure(image="")
            self.qr_photo = None
            if self.host is not None:
                self.status_var.set(
                    "PC 네트워크 연결을 기다리고 있습니다. 주소가 복구되면 QR이 갱신됩니다.",
                )
        else:
            address_changed = self.host is not None and host != self.host
            if address_changed or not self._pairing_qr_available:
                self._render_pairing_qr(host)
            self.host = host
            self._pairing_qr_available = True
            self.address_var.set(
                f"{self.config.display_name} · {host}:{self.config.port}",
            )
            if address_changed:
                self.status_var.set(
                    "PC 주소가 바뀌었습니다. A 태블릿에서 새 QR로 다시 페어링하세요.",
                )
                tray = self.tray
                if tray is not None:
                    try:
                        tray.notify(
                            "PC 주소가 바뀌었습니다. 새 페어링 QR을 확인하세요.",
                            APP_TITLE,
                        )
                    except (NotImplementedError, RuntimeError):
                        pass
        finally:
            if not self._shutting_down:
                self.root.after(LAN_ADDRESS_REFRESH_MS, self._refresh_lan_address)

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

    def choose_csv(self) -> None:
        selected = filedialog.askopenfilename(
            parent=self.root,
            title="학생 CSV 선택",
            filetypes=[("CSV 파일", "*.csv")],
        )
        if not selected:
            return
        path = Path(selected)
        try:
            payload = bytearray(path.read_bytes())
            self.receiver_state.queue_csv(path.name, payload)
        except (OSError, UnicodeError, ValueError) as error:
            messagebox.showerror(APP_TITLE, str(error), parent=self.root)
            return
        finally:
            if "payload" in locals():
                payload[:] = b"\x00" * len(payload)
        self.csv_var.set(f"학생 CSV: {path.name} · 태블릿 요청 대기")
        self.status_var.set("A 태블릿 관리자 화면에서 CSV 가져오기를 누르세요.")

    def clear_csv(self) -> None:
        self.receiver_state.clear_csv()
        self.csv_var.set("학생 CSV: 대기 파일 없음")

    def _poll_events(self) -> None:
        try:
            for _ in range(MAX_EVENTS_PER_POLL):
                event = self.events.get_nowait()
                if event.kind == "fatal":
                    self.show_window()
                    messagebox.showerror(APP_TITLE, event.message, parent=self.root)
                    self.shutdown()
                    return
                self.status_var.set(event.message)
                if event.kind == "status":
                    self.kiosk_state_var.set(f"태블릿 상태: {event.state or '알 수 없음'}")
                    self.student_var.set(
                        f"학생: {event.student_name}" if event.student_name else "학생: 없음",
                    )
                elif event.kind == "csv_sent" and event.accepted:
                    self.csv_var.set("학생 CSV: 전송 완료 · 태블릿 적용 확인 대기")
                elif event.kind == "csv_confirmed" and event.accepted:
                    self.csv_var.set("학생 CSV: 적용 확인 완료 · 대기 파일 없음")
                if event.notify and event.notification_message:
                    try:
                        self.tray.notify(event.notification_message, APP_TITLE)
                    except (NotImplementedError, RuntimeError):
                        pass
                if event.accepted and event.kind != "status":
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
        if self._shutting_down:
            return
        self._shutting_down = True
        self._cleanup_resources()

    def _cleanup_resources(self) -> None:
        tray = self.tray
        if tray is not None:
            try:
                tray.stop()
            except Exception:
                pass
        server = self.server
        server_thread = self.server_thread
        if server is not None:
            try:
                if server_thread is not None and server_thread.is_alive():
                    server.shutdown()
            except Exception:
                pass
            try:
                server.server_close()
            except Exception:
                pass
        receiver_state = self.receiver_state
        if receiver_state is not None:
            receiver_state.close()
        current_thread = threading.current_thread()
        for worker in (server_thread, self.tray_thread):
            if worker is not None and worker is not current_thread and worker.is_alive():
                worker.join(timeout=2)
        root = self.root
        if root is not None:
            try:
                root.destroy()
            except tk.TclError:
                pass


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=APP_TITLE)
    parser.add_argument(
        "--background",
        action="store_true",
        help="Start minimized to the notification area",
    )
    parser.add_argument(
        "--smoke-check",
        action="store_true",
        help=argparse.SUPPRESS,
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.smoke_check:
        from matholic_pdf_receiver.smoke import main as run_smoke_check

        run_smoke_check()
        return
    try:
        application = ReceiverApplication(show_window=not args.background)
    except (OSError, RuntimeError, ValueError) as error:
        root = tk.Tk()
        root.withdraw()
        messagebox.showerror(APP_TITLE, f"수신기를 시작하지 못했습니다.\n\n{error}")
        root.destroy()
        raise SystemExit(1) from error
    try:
        application.run()
    finally:
        application.shutdown()


if __name__ == "__main__":
    main()
