package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiagnosticEventPolicyTest {
    @Test
    fun `interruption event includes only recoverable state`() {
        assertEquals(
            "INTERRUPTED_SESSION:LOGIN_SUBMIT",
            DiagnosticEventPolicy.interruptedSession(WebPocState.LOGIN_SUBMIT),
        )
        assertNull(DiagnosticEventPolicy.interruptedSession(WebPocState.IDLE))
    }

    @Test
    fun `slow stage excludes active grading time and short processing`() {
        assertNull(DiagnosticEventPolicy.slowStage(WebPocState.ACTIVE, 3_600_000L))
        assertNull(DiagnosticEventPolicy.slowStage(WebPocState.LOGIN_FILL, 7_999L))
        assertEquals(
            "SLOW_STAGE:LOGIN_FILL:8S",
            DiagnosticEventPolicy.slowStage(WebPocState.LOGIN_FILL, 8_000L),
        )
    }

    @Test
    fun `result event allowlists reason and bounds structural counts`() {
        assertEquals(
            "RESULT_INCOMPLETE:UNKNOWN:E999:C0:H4:F5",
            DiagnosticEventPolicy.resultIncomplete(
                reason = "student-answer-secret",
                expectedProblems = 2_000,
                classifiedCount = -2,
                hydrationPolls = 4,
                extractionFailures = 5,
            ),
        )
        assertEquals(
            "RESULT_INCOMPLETE:HYDRATING_RESULT:E25:C20:H120:F0",
            DiagnosticEventPolicy.resultIncomplete(
                reason = "HYDRATING_RESULT",
                expectedProblems = 25,
                classifiedCount = 20,
                hydrationPolls = 120,
                extractionFailures = 0,
            ),
        )
    }
}
