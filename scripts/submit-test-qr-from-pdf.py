#!/usr/bin/env python3
"""Submit only the exact `테스트` card from a PDF to A's remote test path.

The QR payload and derived hash stay in process memory and are never printed or
written to disk. Rendered pages live only in memory.
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import subprocess
from pathlib import Path

import cv2
import numpy as np
import pypdfium2 as pdfium
from pypdf import PdfReader

cv2.setLogLevel(0)


EXPECTED_DISPLAY_NAME = "테스트"
EXPECTED_MODEL = "SM-P610"
KIOSK_RECEIVER = "com.local.matholickiosk.kiosk/.AdbRemoteSupportReceiver"
TEST_QR_ACTION = "com.local.matholickiosk.kiosk.action.TEST_QR_HASH"


def run_adb(adb: Path, serial: str, *arguments: str) -> str:
    completed = subprocess.run(
        [str(adb), "-s", serial, *arguments],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    return completed.stdout.strip()


def extract_matholic_payloads(pdf_path: Path) -> set[str]:
    payloads: set[str] = set()
    document = pdfium.PdfDocument(str(pdf_path))
    try:
        for page in document:
            bitmap = page.render(scale=4)
            rgb = np.asarray(bitmap.to_pil().convert("RGB"))
            bgr = cv2.cvtColor(rgb, cv2.COLOR_RGB2BGR)
            detector = cv2.QRCodeDetector()
            multi_ok, decoded, _, _ = detector.detectAndDecodeMulti(bgr)
            candidates = decoded if multi_ok else ()
            if not candidates:
                single, _, _ = detector.detectAndDecode(bgr)
                candidates = (single,) if single else ()
            payloads.update(value for value in candidates if value.startswith("MQR1:"))
    finally:
        document.close()
    return payloads


def derive_hash(payload: str) -> str:
    encoded = payload.removeprefix("MQR1:")
    if len(encoded) != 43:
        raise RuntimeError("The test PDF contains a malformed Matholic QR token.")
    token = bytearray(base64.urlsafe_b64decode(encoded + "="))
    try:
        if len(token) != 32:
            raise RuntimeError("The test PDF contains a malformed Matholic QR token.")
        digest = hashlib.sha256(token).digest()
        return base64.b64encode(digest).decode("ascii")
    finally:
        token[:] = b"\x00" * len(token)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--pdf", required=True, type=Path)
    parser.add_argument("--adb", required=True, type=Path)
    parser.add_argument("--serial", default="R54TB029FHZ")
    args = parser.parse_args()

    if not args.pdf.is_file():
        raise RuntimeError(f"Test QR PDF not found: {args.pdf}")
    if not args.adb.is_file():
        raise RuntimeError(f"ADB not found: {args.adb}")

    extracted_lines = {
        line.strip()
        for page in PdfReader(str(args.pdf)).pages
        for line in (page.extract_text() or "").splitlines()
        if line.strip()
    }
    if EXPECTED_DISPLAY_NAME not in extracted_lines:
        raise RuntimeError("The PDF does not identify an exact 테스트 card.")

    payloads = extract_matholic_payloads(args.pdf)
    if len(payloads) != 1:
        raise RuntimeError(
            f"Expected exactly one Matholic QR in the test PDF, found {len(payloads)}."
        )

    if run_adb(args.adb, args.serial, "get-state") != "device":
        raise RuntimeError("A is not available as an authorized ADB device.")
    model = run_adb(args.adb, args.serial, "shell", "getprop", "ro.product.model")
    if model != EXPECTED_MODEL:
        raise RuntimeError(f"Unexpected device model: {model}")

    encoded_hash = derive_hash(payloads.pop())
    try:
        output = run_adb(
            args.adb,
            args.serial,
            "shell",
            "am",
            "broadcast",
            "-a",
            TEST_QR_ACTION,
            "-n",
            KIOSK_RECEIVER,
            "--es",
            "token_hash_base64",
            encoded_hash,
        )
    finally:
        encoded_hash = ""
    if "result=0" not in output:
        raise RuntimeError("A rejected the remote test QR broadcast.")

    print("TEST_CARD_NAME=VERIFIED_EXACT")
    print("REMOTE_QR_TEST=SUBMITTED")


if __name__ == "__main__":
    main()
