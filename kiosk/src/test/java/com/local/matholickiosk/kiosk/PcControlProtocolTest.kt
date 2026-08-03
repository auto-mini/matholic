package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.transfer.PcControlProtocol
import com.local.matholickiosk.kiosk.transfer.PcReceiverPairing
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcControlProtocolTest {
    private val pairingText =
        "MATHOLIC-PC1:" +
            "AQARIjNEVWZ3iJmqu8zd7v8AAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eH7wB" +
            "DzE5Mi4xNjguMjE5LjIyNAtNQVRIT0xJQy1QQw"
    private val requestId = "102132435465768798a9babbdcddedef".hex()

    @Test
    fun `matches Python status request vector`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val request = PcControlProtocol.encodeRequest(
                pairing = pairing,
                operation = PcControlProtocol.OP_STATUS,
                label = "ACTIVE",
                payload =
                    "{\"state\":\"문제풀이\",\"studentName\":\"테스트\",\"notify\":false}"
                        .toByteArray(),
                timestamp = 1_800_000_000,
                requestId = requestId,
                nonce = "00112233445566778899aabb".hex(),
            )
            assertEquals(
                "TUFUSENUTDEBABEiM0RVZneImaq7zN3u/xAhMkNUZXaHmKm6u9zd7e8AAAAAa0nSAAARIjNE" +
                    "VWZ3iJmquwAAAF6DcOAjF2t5ADT4PGpVh0GCNjbVkFLMm6BfEikprrVPrkj19ipxAOKq0" +
                    "gk81FOxP1SlYHHsMrCCudB9YJyzCGJP6D8+qalQHxqRu8Y77p71BmwxIiVKO+paAWzAGnrv",
                Base64.getEncoder().encodeToString(request.frame),
            )
        } finally {
            pairing.clearSensitiveData()
        }
    }

    @Test
    fun `decodes Python encrypted CSV response vector`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val frame = Base64.getDecoder().decode(
                "TUFUSFJTUDEBABEiM0RVZneImaq7zN3u/xAhMkNUZXaHmKm6u9zd7e8AAAAAa0nSChAhMkN" +
                    "UZXaHmKm6uwAAAEPmyZb3rXE7cTbHZa5DdeDP84GtW23tmmfHL6CC6zHfxiNdBSf3VDescc" +
                    "CePpmz95/hl3EoTa7pR+0X59luebVJsDX1",
            )
            assertEquals(frame.size, PcControlProtocol.responseFrameLength(frame.copyOfRange(0, 65)))
            val response = PcControlProtocol.decodeResponse(
                pairing,
                frame,
                requestId,
                nowEpochSeconds = 1_800_000_020,
            )
            assertTrue(response.accepted)
            assertEquals(PcControlProtocol.OP_FETCH_CSV, response.operation)
            assertEquals("students.csv", response.label)
            assertArrayEquals("이름,아이디\n테스트,test".toByteArray(), response.payload)
        } finally {
            pairing.clearSensitiveData()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects tampered control response`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val frame = Base64.getDecoder().decode(
                "TUFUSFJTUDEBABEiM0RVZneImaq7zN3u/xAhMkNUZXaHmKm6u9zd7e8AAAAAa0nSChAhMkN" +
                    "UZXaHmKm6uwAAAEPmyZb3rXE7cTbHZa5DdeDP84GtW23tmmfHL6CC6zHfxiNdBSf3VDescc" +
                    "CePpmz95/hl3EoTa7pR+0X59luebVJsDX1",
            )
            frame[frame.lastIndex] = (frame.last() xor 1)
            PcControlProtocol.decodeResponse(
                pairing,
                frame,
                requestId,
                nowEpochSeconds = 1_800_000_020,
            )
        } finally {
            pairing.clearSensitiveData()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects authenticated response bound to another request`() {
        val pairing = PcReceiverPairing.decode(pairingText)
        try {
            val frame = Base64.getDecoder().decode(
                "TUFUSFJTUDEBABEiM0RVZneImaq7zN3u/xAhMkNUZXaHmKm6u9zd7e8AAAAAa0nSChAhMkN" +
                    "UZXaHmKm6uwAAAEPmyZb3rXE7cTbHZa5DdeDP84GtW23tmmfHL6CC6zHfxiNdBSf3VDescc" +
                    "CePpmz95/hl3EoTa7pR+0X59luebVJsDX1",
            )
            PcControlProtocol.decodeResponse(
                pairing,
                frame,
                ByteArray(16) { 0x7f.toByte() },
                nowEpochSeconds = 1_800_000_020,
            )
        } finally {
            pairing.clearSensitiveData()
        }
    }

    private fun String.hex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private infix fun Byte.xor(other: Int): Byte = (toInt() xor other).toByte()
}
