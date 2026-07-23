package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import com.local.matholickiosk.kiosk.qr.QrFrameRejection
import com.local.matholickiosk.kiosk.qr.QrParseResult
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrTokenCodecTest {
    private val codec = QrTokenCodec()

    @Test
    fun issueCreatesStrictMqr1PayloadAndOnlyHashIsNeededForLookup() {
        val first = codec.issue()
        val second = codec.issue()

        assertTrue(first.payload.matches(Regex("^MQR1:[A-Za-z0-9_-]{43}$")))
        assertEquals(32, first.hash.size)
        assertNotEquals(first.payload, second.payload)
        assertFalse(first.hash.contentEquals(second.hash))
        val parsed = codec.parse(first.payload) as QrParseResult.Valid
        assertArrayEquals(first.hash, parsed.hash)
    }

    @Test
    fun hashOnlyIssuanceCreatesAnUnlinkableRevocationReplacement() {
        val issued = codec.issue()
        val replacement = codec.issueHashOnly()

        assertEquals(32, replacement.size)
        assertFalse(issued.hash.contentEquals(replacement))
        assertFalse(replacement.contentEquals(codec.issueHashOnly()))
    }

    @Test
    fun nonMqrContentIsIgnoredAndMalformedMqrIsRejected() {
        assertEquals(QrParseResult.Ignore, codec.parse("https://example.test"))
        assertTrue(codec.parse("MQR1:short") is QrParseResult.Invalid)
        assertEquals(QrFrameDecision.Ignore, codec.decideFrame(listOf(null, "ordinary")))
        assertEquals(
            QrFrameDecision.Reject(QrFrameRejection.INVALID_QR),
            codec.decideFrame(listOf("MQR1:short")),
        )
    }

    @Test
    fun multipleMqrCandidatesAreRejectedWithoutSelectingOne() {
        val first = codec.issue()
        val second = codec.issue()

        assertEquals(
            QrFrameDecision.Reject(QrFrameRejection.MULTIPLE_QR),
            codec.decideFrame(listOf(first.payload, second.payload, "ordinary")),
        )
    }
}
