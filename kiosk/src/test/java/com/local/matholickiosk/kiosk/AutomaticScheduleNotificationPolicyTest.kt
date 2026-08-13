package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.AutomaticScheduleNotificationPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticScheduleNotificationPolicyTest {
    @Test
    fun firstAndChangedProblemsNotifyImmediately() {
        val first = AutomaticScheduleNotificationPolicy.onProblem("CLOCK", 1_000L, null)
        assertTrue(first.notify)

        val changed = AutomaticScheduleNotificationPolicy.onProblem(
            "CLASS_MISSING",
            2_000L,
            first.state,
        )
        assertTrue(changed.notify)
    }

    @Test
    fun sameProblemNotifiesAtMostOncePerTenMinutes() {
        val first = AutomaticScheduleNotificationPolicy.onProblem("CLOCK", 1_000L, null)
        val early = AutomaticScheduleNotificationPolicy.onProblem(
            "CLOCK",
            1_000L + AutomaticScheduleNotificationPolicy.REPEAT_INTERVAL_MS - 1L,
            first.state,
        )
        assertFalse(early.notify)

        val due = AutomaticScheduleNotificationPolicy.onProblem(
            "CLOCK",
            1_000L + AutomaticScheduleNotificationPolicy.REPEAT_INTERVAL_MS,
            early.state,
        )
        assertTrue(due.notify)
    }
}
