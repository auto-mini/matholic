package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.FixedClassSlots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedClassSlotsTest {
    @Test
    fun `fixed slots contain two classes for monday through saturday`() {
        assertEquals(
            listOf(
                "월1", "월2", "화1", "화2", "수1", "수2",
                "목1", "목2", "금1", "금2", "토1", "토2",
            ),
            FixedClassSlots.names,
        )
        assertEquals(12, FixedClassSlots.names.distinct().size)
        assertTrue(FixedClassSlots.contains("월1"))
        assertFalse(FixedClassSlots.contains("테스트반"))
        assertEquals(1, FixedClassSlots.isoDayOfWeek("월1"))
        assertEquals(6, FixedClassSlots.isoDayOfWeek("토2"))
        assertEquals(null, FixedClassSlots.isoDayOfWeek("테스트반"))
    }
}
