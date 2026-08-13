package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.AutomaticClassClockPolicy
import com.local.matholickiosk.kiosk.domain.AutomaticClassSchedulePolicy
import com.local.matholickiosk.kiosk.domain.AutomaticClassTransition
import com.local.matholickiosk.kiosk.domain.AutomaticClassTransitionPolicy
import com.local.matholickiosk.kiosk.domain.AutomaticSessionState
import com.local.matholickiosk.kiosk.domain.DailyClassScheduleOverride
import com.local.matholickiosk.kiosk.domain.ScheduledClassEntry
import com.local.matholickiosk.kiosk.domain.ScheduledClassTarget
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticClassSchedulePolicyTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val monday = LocalDate.of(2026, 8, 17)
    private val weekly = listOf(
        ScheduledClassEntry("월1", 1, 14 * 60),
        ScheduledClassEntry("월2", 1, 18 * 60),
    )

    @Test
    fun oneStartTimeCreatesAnExactThreeHourWindow() {
        assertActive(at(14, 0), "월1")
        assertActive(at(16, 59), "월1")
        assertEquals(
            ScheduledClassTarget.Idle,
            AutomaticClassSchedulePolicy.targetAt(at(17, 0), weekly),
        )
        assertActive(at(18, 0), "월2")
        assertEquals(
            ScheduledClassTarget.Idle,
            AutomaticClassSchedulePolicy.targetAt(at(21, 0), weekly),
        )
    }

    @Test
    fun classMayCrossMidnightAndRemainActiveOnTheNextDay() {
        val late = listOf(ScheduledClassEntry("월1", 1, 23 * 60))
        val tuesday = monday.plusDays(1).atTime(1, 59).atZone(zone)
        val target = AutomaticClassSchedulePolicy.targetAt(tuesday, late)
        assertEquals("월1", (target as ScheduledClassTarget.Active).window.className)
        assertEquals(
            ScheduledClassTarget.Idle,
            AutomaticClassSchedulePolicy.targetAt(
                monday.plusDays(1).atTime(2, 0).atZone(zone),
                late,
            ),
        )
    }

    @Test
    fun nextWindowFindsTomorrowAndWrapsFromSaturdayToMonday() {
        val everyWeek = listOf(
            ScheduledClassEntry("월1", 1, 10 * 60),
            ScheduledClassEntry("금1", 5, 10 * 60),
        )
        val thursdayAfterClass = LocalDate.of(2026, 8, 13)
            .atTime(17, 30)
            .atZone(zone)
        val friday = requireNotNull(
            AutomaticClassSchedulePolicy.nextWindowAfter(thursdayAfterClass, everyWeek),
        )
        assertEquals("금1", friday.className)
        assertEquals(LocalDate.of(2026, 8, 14), friday.start.toLocalDate())

        val saturday = LocalDate.of(2026, 8, 15).atTime(17, 30).atZone(zone)
        val mondayWindow = requireNotNull(
            AutomaticClassSchedulePolicy.nextWindowAfter(saturday, everyWeek),
        )
        assertEquals("월1", mondayWindow.className)
        assertEquals(LocalDate.of(2026, 8, 17), mondayWindow.start.toLocalDate())
    }

    @Test
    fun nextWindowHonorsTodayOverrideAndTodayDisable() {
        val now = monday.atTime(9, 0).atZone(zone)
        val override = DailyClassScheduleOverride(
            monday,
            listOf(ScheduledClassEntry("금1", 1, 11 * 60)),
        )
        assertEquals(
            "금1",
            AutomaticClassSchedulePolicy.nextWindowAfter(
                now,
                weekly,
                todayOverride = override,
            )?.className,
        )
        assertEquals(
            "월1",
            AutomaticClassSchedulePolicy.nextWindowAfter(
                now,
                weekly,
                todayOverride = override,
                todayDisabled = true,
            )?.className,
        )
    }

    @Test
    fun todayOverrideReplacesOnlyTodaysWeeklyStarts() {
        val override = DailyClassScheduleOverride(
            monday,
            listOf(ScheduledClassEntry("금1", 1, 15 * 60)),
        )
        assertEquals(
            ScheduledClassTarget.Idle,
            AutomaticClassSchedulePolicy.targetAt(at(14, 30), weekly, override),
        )
        val target = AutomaticClassSchedulePolicy.targetAt(at(15, 0), weekly, override)
        assertEquals("금1", (target as ScheduledClassTarget.Active).window.className)
    }

    @Test
    fun validationRejectsOverlappingThreeHourClassesIncludingWeekBoundary() {
        val sameDay = listOf(
            ScheduledClassEntry("월1", 1, 14 * 60),
            ScheduledClassEntry("월2", 1, 16 * 60),
        )
        assertTrue(AutomaticClassSchedulePolicy.validateWeekly(sameDay).any {
            it.contains("겹칩니다")
        })
        val weekBoundary = listOf(
            ScheduledClassEntry("토2", 6, 23 * 60),
            ScheduledClassEntry("월1", 1, 1 * 60),
        )
        assertFalse(AutomaticClassSchedulePolicy.validateWeekly(weekBoundary).any {
            it.contains("겹칩니다")
        })
    }

    @Test
    fun busyStudentAndAdministratorWorkAlwaysDeferTransitions() {
        val active = AutomaticClassSchedulePolicy.targetAt(at(14, 0), weekly)
        assertEquals(
            AutomaticClassTransition.Deferred,
            AutomaticClassTransitionPolicy.decide(
                active,
                AutomaticSessionState.STUDENT_BUSY,
                "월2",
                administratorWorkVisible = false,
                operationInProgress = false,
            ),
        )
        assertEquals(
            AutomaticClassTransition.Deferred,
            AutomaticClassTransitionPolicy.decide(
                ScheduledClassTarget.Idle,
                AutomaticSessionState.QR_READY,
                "월1",
                administratorWorkVisible = true,
                operationInProgress = false,
            ),
        )
    }

    @Test
    fun idleStartsMatchingReadyDoesNothingAndEndTimeEnds() {
        val active = AutomaticClassSchedulePolicy.targetAt(at(14, 0), weekly)
        assertEquals(
            AutomaticClassTransition.Start("월1"),
            AutomaticClassTransitionPolicy.decide(
                active,
                AutomaticSessionState.IDLE,
                null,
                false,
                false,
            ),
        )
        assertEquals(
            AutomaticClassTransition.None,
            AutomaticClassTransitionPolicy.decide(
                active,
                AutomaticSessionState.QR_READY,
                "월1",
                false,
                false,
            ),
        )
        assertEquals(
            AutomaticClassTransition.End,
            AutomaticClassTransitionPolicy.decide(
                ScheduledClassTarget.Idle,
                AutomaticSessionState.QR_READY,
                "월1",
                false,
                false,
            ),
        )
    }

    @Test
    fun actionableTransitionIsDeferredWhenUiStateChangesBeforeApply() {
        val transition = AutomaticClassTransition.Switch("월2")
        assertTrue(
            AutomaticClassTransitionPolicy.shouldDeferBeforeApply(
                transition = transition,
                evaluatedSessionState = AutomaticSessionState.QR_READY,
                currentSessionState = AutomaticSessionState.QR_READY,
                administratorWorkVisible = true,
                operationInProgress = false,
            ),
        )
        assertTrue(
            AutomaticClassTransitionPolicy.shouldDeferBeforeApply(
                transition = transition,
                evaluatedSessionState = AutomaticSessionState.QR_READY,
                currentSessionState = AutomaticSessionState.STUDENT_BUSY,
                administratorWorkVisible = false,
                operationInProgress = false,
            ),
        )
        assertTrue(
            AutomaticClassTransitionPolicy.shouldDeferBeforeApply(
                transition = transition,
                evaluatedSessionState = AutomaticSessionState.QR_READY,
                currentSessionState = AutomaticSessionState.QR_READY,
                administratorWorkVisible = false,
                operationInProgress = true,
            ),
        )
        assertFalse(
            AutomaticClassTransitionPolicy.shouldDeferBeforeApply(
                transition = transition,
                evaluatedSessionState = AutomaticSessionState.QR_READY,
                currentSessionState = AutomaticSessionState.QR_READY,
                administratorWorkVisible = false,
                operationInProgress = false,
            ),
        )
    }

    @Test
    fun implausibleOrRolledBackClockIsRejected() {
        val now = at(14, 0)
        assertTrue(AutomaticClassClockPolicy.isPlausible(now, null))
        assertFalse(
            AutomaticClassClockPolicy.isPlausible(
                now,
                now.plusMinutes(6).toInstant().toEpochMilli(),
            ),
        )
        assertFalse(
            AutomaticClassClockPolicy.isPlausible(
                ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, zone),
                null,
            ),
        )
    }

    private fun assertActive(time: ZonedDateTime, className: String) {
        val target = AutomaticClassSchedulePolicy.targetAt(time, weekly)
        assertEquals(className, (target as ScheduledClassTarget.Active).window.className)
        assertEquals(180, java.time.Duration.between(target.window.start, target.window.endExclusive).toMinutes())
    }

    private fun at(hour: Int, minute: Int): ZonedDateTime =
        monday.atTime(hour, minute).atZone(zone)
}
