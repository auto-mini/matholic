package com.local.matholickiosk.kiosk

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.local.matholickiosk.kiosk.domain.DailyClassScheduleOverride
import com.local.matholickiosk.kiosk.domain.ScheduledClassEntry
import java.time.LocalDate
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutomaticClassScheduleStoreInstrumentedTest {
    @Test
    fun weeklyTodayOverrideAndTodayDisableRoundTripWithoutCredentials() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteSharedPreferences(PREFERENCES_NAME)
        val store = AutomaticClassScheduleStore(context)
        val date = LocalDate.of(2026, 8, 17)
        try {
            store.saveWeekly(
                StoredClassSchedule(
                    startMinuteByClass = mapOf("월1" to 14 * 60, "월2" to 18 * 60),
                    enabledClassNames = setOf("월1", "월2"),
                ),
            )
            assertEquals(setOf("월1", "월2"), store.loadWeekly().enabledClassNames)
            assertTrue(store.requiresManualOverride(date))

            store.saveOverride(
                DailyClassScheduleOverride(
                    date,
                    listOf(ScheduledClassEntry("금1", 1, 15 * 60)),
                ),
            )
            assertEquals("금1", store.loadOverride(date)?.entries?.single()?.className)

            store.disableForToday(date)
            assertTrue(store.isDisabledFor(date))
            assertFalse(store.requiresManualOverride(date))
            store.enableForToday(date)
            assertTrue(store.requiresManualOverride(date))

            val now = ZonedDateTime.parse("2026-08-17T14:00:00+09:00[Asia/Seoul]")
            store.trustCurrentClock(now)
            assertTrue(store.isClockPlausible(now.plusMinutes(1)))
            assertFalse(store.isClockPlausible(now.minusMinutes(6)))
        } finally {
            context.deleteSharedPreferences(PREFERENCES_NAME)
        }
    }

    @Test
    fun administratorLayoutExposesScheduleControlAndReadableSummary() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        lateinit var root: View
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
        }
        val button = root.findViewById<Button>(R.id.class_schedule_button)
        val summary = root.findViewById<TextView>(R.id.class_schedule_summary)
        val closeAdminButton = root.findViewById<Button>(R.id.close_admin_button)

        assertEquals("자동 반 시간표 설정", button.text.toString())
        assertTrue(summary.text.toString().contains("설정 안 됨"))
        assertTrue(button.filterTouchesWhenObscured)
        assertEquals("관리자 화면 잠그고 자동 대기", closeAdminButton.text.toString())
        assertTrue(closeAdminButton.filterTouchesWhenObscured)
    }

    @Test
    fun emptyClassSkipNotificationIsConsumedOncePerScheduledWindowAcrossStoreInstances() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteSharedPreferences(PREFERENCES_NAME)
        try {
            val firstStore = AutomaticClassScheduleStore(context)
            assertTrue(firstStore.consumeEmptyClassSkipNotification("토1:1787360400000"))
            assertFalse(firstStore.consumeEmptyClassSkipNotification("토1:1787360400000"))

            val recreatedStore = AutomaticClassScheduleStore(context)
            assertFalse(recreatedStore.consumeEmptyClassSkipNotification("토1:1787360400000"))
            assertTrue(recreatedStore.consumeEmptyClassSkipNotification("토2:1787371200000"))
        } finally {
            context.deleteSharedPreferences(PREFERENCES_NAME)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "automatic_class_schedule"
    }
}
