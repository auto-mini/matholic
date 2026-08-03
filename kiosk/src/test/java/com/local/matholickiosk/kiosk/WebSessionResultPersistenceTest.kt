package com.local.matholickiosk.kiosk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSessionResultPersistenceTest {
    @Test
    fun transitionAndSessionReloadMustBothSucceed() {
        var transitionCalls = 0

        val result = WebSessionResultPersistence.persist(
            passed = true,
            persistTransition = { transitionCalls += 1 },
            loadSession = { "QR_READY" },
        )

        assertTrue(result.isSuccess)
        assertEquals(1, transitionCalls)
        assertEquals(
            PersistedWebSessionResult(passed = true, session = "QR_READY"),
            result.getOrThrow(),
        )
    }

    @Test
    fun sessionReloadFailureCannotBeReportedAsSuccessfulReturn() {
        val result = WebSessionResultPersistence.persist<String>(
            passed = true,
            persistTransition = {},
            loadSession = { error("synthetic session reload failure") },
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun missingSessionCannotBeReportedAsSuccessfulReturn() {
        val result = WebSessionResultPersistence.persist<String>(
            passed = false,
            persistTransition = {},
            loadSession = { null },
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun failedTransitionDoesNotAttemptSessionReload() {
        var loadAttempted = false

        val result = WebSessionResultPersistence.persist<String>(
            passed = false,
            persistTransition = { error("synthetic transition failure") },
            loadSession = {
                loadAttempted = true
                "LOCKED"
            },
        )

        assertTrue(result.isFailure)
        assertFalse(loadAttempted)
    }
}
