package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.StudentLoginPcNotificationPolicy
import com.local.matholickiosk.kiosk.domain.StudentLoginPcStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentLoginPcNotificationPolicyTest {
    @Test
    fun `notifies exactly once across the student login stages`() {
        val notifications = StudentLoginPcStage.entries.count(
            StudentLoginPcNotificationPolicy::shouldNotify,
        )

        assertEquals(1, notifications)
        assertTrue(
            StudentLoginPcNotificationPolicy.shouldNotify(
                StudentLoginPcStage.QR_VERIFIED,
            ),
        )
        assertFalse(
            StudentLoginPcNotificationPolicy.shouldNotify(
                StudentLoginPcStage.LOGIN_IN_PROGRESS,
            ),
        )
    }
}
