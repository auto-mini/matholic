package com.local.matholickiosk.kiosk.domain

import java.time.LocalDate
import java.time.ZonedDateTime

data class ScheduledClassEntry(
    val className: String,
    val isoDayOfWeek: Int,
    val startMinuteOfDay: Int,
)

data class DailyClassScheduleOverride(
    val date: LocalDate,
    val entries: List<ScheduledClassEntry>,
)

data class ScheduledClassWindow(
    val className: String,
    val start: ZonedDateTime,
    val endExclusive: ZonedDateTime,
)

sealed interface ScheduledClassTarget {
    data object Idle : ScheduledClassTarget
    data class Active(val window: ScheduledClassWindow) : ScheduledClassTarget
}

enum class AutomaticSessionState {
    IDLE,
    QR_READY,
    STUDENT_BUSY,
    RECOVERY_REQUIRED,
}

sealed interface AutomaticClassTransition {
    data object None : AutomaticClassTransition
    data class Start(val className: String) : AutomaticClassTransition
    data class Switch(val className: String) : AutomaticClassTransition
    data object End : AutomaticClassTransition
    data object Deferred : AutomaticClassTransition
}

enum class AutomaticEmptyClassAction {
    PROCEED,
    SKIP_START,
    END_CURRENT_AND_SKIP,
}

object AutomaticEmptyClassPolicy {
    fun decide(
        transition: AutomaticClassTransition,
        targetHasActiveStudents: Boolean?,
    ): AutomaticEmptyClassAction {
        if (targetHasActiveStudents != false) return AutomaticEmptyClassAction.PROCEED
        return when (transition) {
            is AutomaticClassTransition.Start -> AutomaticEmptyClassAction.SKIP_START
            is AutomaticClassTransition.Switch ->
                AutomaticEmptyClassAction.END_CURRENT_AND_SKIP
            else -> AutomaticEmptyClassAction.PROCEED
        }
    }
}

object AutomaticClassSchedulePolicy {
    const val CLASS_DURATION_MINUTES = 180
    private const val MINUTES_PER_DAY = 24 * 60
    private const val MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY

    fun validateWeekly(entries: List<ScheduledClassEntry>): List<String> {
        val errors = mutableListOf<String>()
        val duplicateNames = entries.groupingBy(ScheduledClassEntry::className)
            .eachCount()
            .filterValues { it > 1 }
            .keys
        duplicateNames.forEach { errors += "$it 시간은 한 번만 설정할 수 있습니다." }
        entries.forEach { entry ->
            if (!FixedClassSlots.contains(entry.className)) {
                errors += "알 수 없는 고정 반입니다: ${entry.className}"
            }
            if (entry.isoDayOfWeek !in 1..6) {
                errors += "${entry.className}의 요일이 올바르지 않습니다."
            }
            if (FixedClassSlots.isoDayOfWeek(entry.className) != entry.isoDayOfWeek) {
                errors += "${entry.className}의 요일과 시간표 요일이 일치하지 않습니다."
            }
            if (entry.startMinuteOfDay !in 0 until MINUTES_PER_DAY) {
                errors += "${entry.className}의 시작 시각이 올바르지 않습니다."
            }
        }
        errors += overlappingPairs(entries).map { (first, second) ->
            "${first.className}과 ${second.className}의 3시간 수업이 겹칩니다."
        }
        return errors.distinct()
    }

    fun validateDailyOverride(
        date: LocalDate,
        entries: List<ScheduledClassEntry>,
    ): List<String> {
        val errors = mutableListOf<String>()
        if (entries.size > 2) errors += "오늘 임시 시간표는 최대 두 수업까지 설정할 수 있습니다."
        if (entries.map(ScheduledClassEntry::className).distinct().size != entries.size) {
            errors += "오늘 임시 시간표에 같은 반을 두 번 넣을 수 없습니다."
        }
        entries.forEach { entry ->
            if (!FixedClassSlots.contains(entry.className)) {
                errors += "알 수 없는 고정 반입니다: ${entry.className}"
            }
            if (entry.isoDayOfWeek != date.dayOfWeek.value) {
                errors += "오늘 임시 시간표의 요일이 현재 날짜와 일치하지 않습니다."
            }
            if (entry.startMinuteOfDay !in 0 until MINUTES_PER_DAY) {
                errors += "${entry.className}의 시작 시각이 올바르지 않습니다."
            }
        }
        errors += overlappingPairs(entries).map { (first, second) ->
            "${first.className}과 ${second.className}의 3시간 수업이 겹칩니다."
        }
        return errors.distinct()
    }

    fun targetAt(
        now: ZonedDateTime,
        weeklyEntries: List<ScheduledClassEntry>,
        todayOverride: DailyClassScheduleOverride? = null,
        yesterdayOverride: DailyClassScheduleOverride? = null,
    ): ScheduledClassTarget {
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        val candidates = buildList {
            addAll(windowsForDate(yesterday, now.zone, weeklyEntries, yesterdayOverride))
            addAll(windowsForDate(today, now.zone, weeklyEntries, todayOverride))
        }
        val active = candidates
            .filter { !now.isBefore(it.start) && now.isBefore(it.endExclusive) }
            .maxByOrNull(ScheduledClassWindow::start)
        return active?.let(ScheduledClassTarget::Active) ?: ScheduledClassTarget.Idle
    }

    fun nextBoundaryAfter(
        now: ZonedDateTime,
        weeklyEntries: List<ScheduledClassEntry>,
        todayOverride: DailyClassScheduleOverride? = null,
        tomorrowOverride: DailyClassScheduleOverride? = null,
    ): ZonedDateTime? {
        val today = now.toLocalDate()
        val boundaries = buildList {
            windowsForDate(today, now.zone, weeklyEntries, todayOverride).forEach {
                add(it.start)
                add(it.endExclusive)
            }
            windowsForDate(today.plusDays(1), now.zone, weeklyEntries, tomorrowOverride).forEach {
                add(it.start)
                add(it.endExclusive)
            }
        }
        return boundaries.filter { it.isAfter(now) }.minOrNull()
    }

    fun nextWindowAfter(
        now: ZonedDateTime,
        weeklyEntries: List<ScheduledClassEntry>,
        todayOverride: DailyClassScheduleOverride? = null,
        todayDisabled: Boolean = false,
    ): ScheduledClassWindow? = (0L..7L)
        .flatMap { dayOffset ->
            val date = now.toLocalDate().plusDays(dayOffset)
            if (dayOffset == 0L && todayDisabled) {
                emptyList()
            } else {
                windowsForDate(
                    date = date,
                    zoneId = now.zone,
                    weeklyEntries = weeklyEntries,
                    override = todayOverride?.takeIf { it.date == date },
                )
            }
        }
        .filter { it.start.isAfter(now) }
        .minByOrNull(ScheduledClassWindow::start)

    private fun windowsForDate(
        date: LocalDate,
        zoneId: java.time.ZoneId,
        weeklyEntries: List<ScheduledClassEntry>,
        override: DailyClassScheduleOverride?,
    ): List<ScheduledClassWindow> {
        val entries = if (override?.date == date) {
            override.entries
        } else {
            weeklyEntries.filter { it.isoDayOfWeek == date.dayOfWeek.value }
        }
        return entries.map { entry ->
            val start = date.atStartOfDay(zoneId)
                .plusMinutes(entry.startMinuteOfDay.toLong())
            ScheduledClassWindow(
                className = entry.className,
                start = start,
                endExclusive = start.plusMinutes(CLASS_DURATION_MINUTES.toLong()),
            )
        }
    }

    private fun overlappingPairs(
        entries: List<ScheduledClassEntry>,
    ): List<Pair<ScheduledClassEntry, ScheduledClassEntry>> {
        val pairs = mutableListOf<Pair<ScheduledClassEntry, ScheduledClassEntry>>()
        entries.forEachIndexed { index, first ->
            entries.drop(index + 1).forEach { second ->
                val firstStart = (first.isoDayOfWeek - 1) * MINUTES_PER_DAY +
                    first.startMinuteOfDay
                val secondStart = (second.isoDayOfWeek - 1) * MINUTES_PER_DAY +
                    second.startMinuteOfDay
                val overlaps = listOf(-MINUTES_PER_WEEK, 0, MINUTES_PER_WEEK).any { shift ->
                    val shiftedSecond = secondStart + shift
                    firstStart < shiftedSecond + CLASS_DURATION_MINUTES &&
                        shiftedSecond < firstStart + CLASS_DURATION_MINUTES
                }
                if (overlaps) pairs += first to second
            }
        }
        return pairs
    }
}

object AutomaticClassTransitionPolicy {
    fun decide(
        target: ScheduledClassTarget,
        sessionState: AutomaticSessionState,
        currentClassName: String?,
        administratorWorkVisible: Boolean,
        operationInProgress: Boolean,
    ): AutomaticClassTransition {
        if (administratorWorkVisible || operationInProgress) {
            return AutomaticClassTransition.Deferred
        }
        return when (target) {
            ScheduledClassTarget.Idle -> when (sessionState) {
                AutomaticSessionState.IDLE -> AutomaticClassTransition.None
                AutomaticSessionState.QR_READY -> AutomaticClassTransition.End
                AutomaticSessionState.STUDENT_BUSY,
                AutomaticSessionState.RECOVERY_REQUIRED,
                -> AutomaticClassTransition.Deferred
            }
            is ScheduledClassTarget.Active -> when (sessionState) {
                AutomaticSessionState.IDLE ->
                    AutomaticClassTransition.Start(target.window.className)
                AutomaticSessionState.QR_READY -> if (
                    currentClassName == target.window.className
                ) {
                    AutomaticClassTransition.None
                } else {
                    AutomaticClassTransition.Switch(target.window.className)
                }
                AutomaticSessionState.STUDENT_BUSY,
                AutomaticSessionState.RECOVERY_REQUIRED,
                -> AutomaticClassTransition.Deferred
            }
        }
    }

    fun shouldDeferBeforeApply(
        transition: AutomaticClassTransition,
        evaluatedSessionState: AutomaticSessionState,
        currentSessionState: AutomaticSessionState,
        administratorWorkVisible: Boolean,
        operationInProgress: Boolean,
    ): Boolean {
        if (
            transition == AutomaticClassTransition.None ||
            transition == AutomaticClassTransition.Deferred
        ) {
            return false
        }
        return administratorWorkVisible ||
            operationInProgress ||
            currentSessionState != evaluatedSessionState
    }
}

object AutomaticClassClockPolicy {
    fun isPlausible(now: ZonedDateTime, lastAcceptedEpochMillis: Long?): Boolean {
        if (now.year !in 2025..2100) return false
        if (lastAcceptedEpochMillis == null) return true
        return now.toInstant().toEpochMilli() >= lastAcceptedEpochMillis - 5 * 60_000L
    }
}
