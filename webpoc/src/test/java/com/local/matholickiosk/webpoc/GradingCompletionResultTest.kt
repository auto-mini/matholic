package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Test

class GradingCompletionResultTest {
    @Test
    fun `completed result keeps only sorted distinct valid problem numbers`() {
        val result = GradingCompletionResult.complete(listOf(5, 2, 5, 0, -1, 10_000, 3))

        assertEquals(GradingCompletionOutcome.COMPLETE, result.outcome)
        assertEquals(listOf(2, 3, 5), result.wrongProblemNumbers)
    }

    @Test
    fun `oversized result fails closed without partial wrong answer detail`() {
        val result = GradingCompletionResult.complete((1..201).toList())

        assertEquals(GradingCompletionOutcome.DETAIL_UNAVAILABLE, result.outcome)
        assertEquals(emptyList<Int>(), result.wrongProblemNumbers)
    }
}
