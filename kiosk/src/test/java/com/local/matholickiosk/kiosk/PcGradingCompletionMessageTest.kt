package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract
import com.local.matholickiosk.kiosk.domain.PcGradingCompletionMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PcGradingCompletionMessageTest {
    @Test
    fun `completed result includes wrong answer count and numbers`() {
        val message = PcGradingCompletionMessage.fromWire(
            CredentialBridgeContract.GRADING_RESULT_COMPLETE,
            intArrayOf(9, 2, 9, 5),
        )

        assertEquals("채점 완료 · 오답 3개: 2, 5, 9번", message)
    }

    @Test
    fun `perfect result reports no wrong problems`() {
        val message = PcGradingCompletionMessage.fromWire(
            CredentialBridgeContract.GRADING_RESULT_COMPLETE,
            intArrayOf(),
        )

        assertEquals("채점 완료 · 틀린 문제 없음", message)
    }

    @Test
    fun `manual or idle exit without a result does not create a completion alert`() {
        assertNull(PcGradingCompletionMessage.fromWire(null, null))
    }

    @Test
    fun `unavailable or oversized detail fails closed`() {
        assertEquals(
            "채점 완료 · 상세 결과 확인 필요",
            PcGradingCompletionMessage.fromWire(
                CredentialBridgeContract.GRADING_RESULT_DETAIL_UNAVAILABLE,
                null,
            ),
        )
        assertEquals(
            "채점 완료 · 상세 결과 확인 필요",
            PcGradingCompletionMessage.fromWire(
                CredentialBridgeContract.GRADING_RESULT_COMPLETE,
                IntArray(201) { it + 1 },
            ),
        )
    }

    @Test
    fun `long result is bounded for the pc protocol`() {
        val message = requireNotNull(
            PcGradingCompletionMessage.fromWire(
                CredentialBridgeContract.GRADING_RESULT_COMPLETE,
                IntArray(25) { it + 1 },
            ),
        )

        assertEquals("채점 완료 · 오답 25개: 1, 2, 3, 4, 5, 6, 7, 8번 외 17개", message)
        assertTrue(message.length <= 80)
    }
}
