from matholic_pdf_receiver.smoke import run_authenticated_roundtrip


class IdentitySecretProtector:
    def protect(self, plaintext: bytes) -> bytes:
        return plaintext

    def unprotect(self, protected: bytes) -> bytes:
        return protected


def test_smoke_roundtrip_uses_an_isolated_ephemeral_server() -> None:
    run_authenticated_roundtrip(IdentitySecretProtector())
