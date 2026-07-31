package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import com.local.matholickiosk.kiosk.transfer.PcTransferProtocol
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PcTransferProtocolTest {
    private val pairingText =
        "MATHOLIC-PC1:" +
            "AQARIjNEVWZ3iJmqu8zd7v8AAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eH7wB" +
            "DzE5Mi4xNjguMjE5LjIyNAtNQVRIT0xJQy1QQw"

    @Test
    fun `decodes Python pairing vector`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            assertArrayEquals(
                "00112233445566778899aabbccddeeff".hex(),
                pairing.receiverId,
            )
            assertArrayEquals(ByteArray(32) { it.toByte() }, pairing.secret)
            assertEquals("192.168.219.224", pairing.host)
            assertEquals(48129, pairing.port)
            assertEquals("MATHOLIC-PC", pairing.displayName)
        } finally {
            pairing.clearSensitiveData()
        }
    }

    @Test
    fun `updated host encoding round trips without changing identity or secret`() {
        val original = PcReceiverPairing.decode(pairingText)
        val updated = original.withHost("192.168.219.230")
        val decoded = PcReceiverPairing.decode(updated.encode())
        try {
            assertEquals("192.168.219.230", decoded.host)
            assertEquals(original.port, decoded.port)
            assertEquals(original.displayName, decoded.displayName)
            assertArrayEquals(original.receiverId, decoded.receiverId)
            assertArrayEquals(original.secret, decoded.secret)
        } finally {
            original.clearSensitiveData()
            updated.clearSensitiveData()
            decoded.clearSensitiveData()
        }
    }

    @Test
    fun `matches Python encrypted request and authenticated ack vectors`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val pdf = "%PDF-1.4\nmatholic-test\n%%EOF\n".toByteArray()
            val transfer = PcTransferProtocol.encodeRequest(
                pairing = pairing,
                filename = "홍길동 QR.pdf",
                pdf = pdf,
                timestamp = 1_800_000_000,
                requestId = "102132435465768798a9babbdcddedef".hex(),
                nonce = "00112233445566778899aabb".hex(),
            )
            assertEquals(
                "TUFUSFBERjEBABEiM0RVZneImaq7zN3u/xAhMkNUZXaHmKm6u9zd7e8AAAAAa0nSAAARIjNE" +
                    "VWZ3iJmquwAAAEOCYOYjF3bV2PpGzYT7c/rREwWPhRSQnBu37Oi4HGzXQ8UcKmcxS/Lz" +
                    "0wgqzi3ae3yPBEFgIsY4D5voPFrJ8beGbkLJ",
                Base64.getEncoder().encodeToString(transfer.frame),
            )
            val ack = Base64.getDecoder().decode(
                "TUFUSEFDSzEBECEyQ1RldoeYqbq73N3t77hn2ZnGMDRqIz6OwjGjo9+xIydW+JCkzl5i" +
                    "5IwFcuk2uBbydtZjUVSJ+X5VFCu7IF2NsAAeNCCKBQkELJUQWU4=",
            )
            PcTransferProtocol.verifyAck(
                pairing,
                ack,
                transfer.requestId,
                transfer.pdfSha256,
            )
        } finally {
            pairing.clearSensitiveData()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects tampered acknowledgement`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val ack = Base64.getDecoder().decode(
                "TUFUSEFDSzEBECEyQ1RldoeYqbq73N3t77hn2ZnGMDRqIz6OwjGjo9+xIydW+JCkzl5i" +
                    "5IwFcuk2uBbydtZjUVSJ+X5VFCu7IF2NsAAeNCCKBQkELJUQWU4=",
            )
            ack[ack.lastIndex] = (ack.last() xor 1)
            PcTransferProtocol.verifyAck(
                pairing,
                ack,
                "102132435465768798a9babbdcddedef".hex(),
                ByteArray(32),
            )
        } finally {
            pairing.clearSensitiveData()
        }
    }

    private fun String.hex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private infix fun Byte.xor(other: Int): Byte = (toInt() xor other).toByte()
}
