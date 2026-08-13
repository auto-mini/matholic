package com.local.matholickiosk.kiosk

import android.content.Context
import com.local.matholickiosk.kiosk.domain.AutomaticClassClockPolicy
import com.local.matholickiosk.kiosk.domain.AutomaticClassSchedulePolicy
import com.local.matholickiosk.kiosk.domain.DailyClassScheduleOverride
import com.local.matholickiosk.kiosk.domain.FixedClassSlots
import com.local.matholickiosk.kiosk.domain.ScheduledClassEntry
import java.time.LocalDate
import java.time.ZonedDateTime

internal data class StoredClassSchedule(
    val startMinuteByClass: Map<String, Int>,
    val enabledClassNames: Set<String>,
) {
    fun enabledEntries(): List<ScheduledClassEntry> = FixedClassSlots.names.mapNotNull { name ->
        val startMinute = startMinuteByClass[name] ?: return@mapNotNull null
        if (name !in enabledClassNames) return@mapNotNull null
        ScheduledClassEntry(
            className = name,
            isoDayOfWeek = requireNotNull(FixedClassSlots.isoDayOfWeek(name)),
            startMinuteOfDay = startMinute,
        )
    }
}

internal class AutomaticClassScheduleStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun loadWeekly(): StoredClassSchedule {
        val starts = buildMap {
            FixedClassSlots.names.forEachIndexed { index, name ->
                val value = preferences.getInt(startKey(index), UNSET_MINUTE)
                if (value != UNSET_MINUTE) {
                    require(value in 0 until MINUTES_PER_DAY) {
                        "저장된 ${name} 시작 시각이 올바르지 않습니다."
                    }
                    put(name, value)
                }
            }
        }
        val enabled = FixedClassSlots.names.filterIndexedTo(mutableSetOf()) { index, name ->
            preferences.getBoolean(enabledKey(index), false).also { isEnabled ->
                require(!isEnabled || name in starts) {
                    "${name}의 시작 시각을 먼저 설정하세요."
                }
            }
        }
        return StoredClassSchedule(starts, enabled)
    }

    fun saveWeekly(schedule: StoredClassSchedule) {
        require(schedule.startMinuteByClass.keys.all(FixedClassSlots::contains))
        require(schedule.enabledClassNames.all(FixedClassSlots::contains))
        val errors = AutomaticClassSchedulePolicy.validateWeekly(schedule.enabledEntries())
        require(errors.isEmpty()) { errors.joinToString("\n") }
        preferences.edit().apply {
            FixedClassSlots.names.forEachIndexed { index, name ->
                val start = schedule.startMinuteByClass[name]
                if (start == null) remove(startKey(index)) else putInt(startKey(index), start)
                putBoolean(enabledKey(index), name in schedule.enabledClassNames)
            }
        }.apply()
    }

    fun loadOverride(date: LocalDate): DailyClassScheduleOverride? {
        if (preferences.getLong(KEY_OVERRIDE_DATE, Long.MIN_VALUE) != date.toEpochDay()) {
            return null
        }
        val count = preferences.getInt(KEY_OVERRIDE_COUNT, 0)
        require(count in 0..MAX_DAILY_CLASSES) { "오늘 임시 시간표가 올바르지 않습니다." }
        val entries = (0 until count).map { index ->
            val className = requireNotNull(preferences.getString(overrideClassKey(index), null)) {
                "오늘 임시 반 정보가 없습니다."
            }
            val start = preferences.getInt(overrideStartKey(index), UNSET_MINUTE)
            ScheduledClassEntry(className, date.dayOfWeek.value, start)
        }
        val errors = AutomaticClassSchedulePolicy.validateDailyOverride(date, entries)
        require(errors.isEmpty()) { errors.joinToString("\n") }
        return DailyClassScheduleOverride(date, entries)
    }

    fun saveOverride(override: DailyClassScheduleOverride) {
        val errors = AutomaticClassSchedulePolicy.validateDailyOverride(
            override.date,
            override.entries,
        )
        require(errors.isEmpty()) { errors.joinToString("\n") }
        preferences.edit().apply {
            putLong(KEY_OVERRIDE_DATE, override.date.toEpochDay())
            putInt(KEY_OVERRIDE_COUNT, override.entries.size)
            repeat(MAX_DAILY_CLASSES) { index ->
                val entry = override.entries.getOrNull(index)
                if (entry == null) {
                    remove(overrideClassKey(index))
                    remove(overrideStartKey(index))
                } else {
                    putString(overrideClassKey(index), entry.className)
                    putInt(overrideStartKey(index), entry.startMinuteOfDay)
                }
            }
        }.apply()
    }

    fun clearOverride(date: LocalDate) {
        if (preferences.getLong(KEY_OVERRIDE_DATE, Long.MIN_VALUE) != date.toEpochDay()) return
        preferences.edit().apply {
            remove(KEY_OVERRIDE_DATE)
            remove(KEY_OVERRIDE_COUNT)
            repeat(MAX_DAILY_CLASSES) { index ->
                remove(overrideClassKey(index))
                remove(overrideStartKey(index))
            }
        }.apply()
    }

    fun disableForToday(date: LocalDate) {
        preferences.edit().putLong(KEY_DISABLED_DATE, date.toEpochDay()).apply()
    }

    fun enableForToday(date: LocalDate) {
        if (preferences.getLong(KEY_DISABLED_DATE, Long.MIN_VALUE) == date.toEpochDay()) {
            preferences.edit().remove(KEY_DISABLED_DATE).apply()
        }
    }

    fun isDisabledFor(date: LocalDate): Boolean =
        preferences.getLong(KEY_DISABLED_DATE, Long.MIN_VALUE) == date.toEpochDay()

    fun hasAutomaticSchedule(date: LocalDate): Boolean =
        loadOverride(date)?.entries?.isNotEmpty() == true || loadWeekly().enabledEntries().isNotEmpty()

    fun requiresManualOverride(date: LocalDate): Boolean =
        hasAutomaticSchedule(date) && !isDisabledFor(date)

    fun isClockPlausible(now: ZonedDateTime): Boolean = AutomaticClassClockPolicy.isPlausible(
        now = now,
        lastAcceptedEpochMillis = preferences
            .getLong(KEY_LAST_ACCEPTED_CLOCK, Long.MIN_VALUE)
            .takeUnless { it == Long.MIN_VALUE },
    )

    fun recordAcceptedClock(now: ZonedDateTime) {
        val epochMillis = now.toInstant().toEpochMilli()
        val previous = preferences.getLong(KEY_LAST_ACCEPTED_CLOCK, Long.MIN_VALUE)
        if (previous == Long.MIN_VALUE || epochMillis > previous) {
            preferences.edit().putLong(KEY_LAST_ACCEPTED_CLOCK, epochMillis).apply()
        }
    }

    fun trustCurrentClock(now: ZonedDateTime) {
        require(now.year in 2025..2100) { "현재 기기 날짜가 올바르지 않습니다." }
        preferences.edit()
            .putLong(KEY_LAST_ACCEPTED_CLOCK, now.toInstant().toEpochMilli())
            .apply()
    }

    private fun startKey(index: Int) = "slot_${index}_start_minute"
    private fun enabledKey(index: Int) = "slot_${index}_enabled"
    private fun overrideClassKey(index: Int) = "override_${index}_class"
    private fun overrideStartKey(index: Int) = "override_${index}_start_minute"

    companion object {
        private const val PREFERENCES_NAME = "automatic_class_schedule"
        private const val KEY_DISABLED_DATE = "disabled_epoch_day"
        private const val KEY_OVERRIDE_DATE = "override_epoch_day"
        private const val KEY_OVERRIDE_COUNT = "override_count"
        private const val KEY_LAST_ACCEPTED_CLOCK = "last_accepted_clock_epoch_ms"
        private const val UNSET_MINUTE = -1
        private const val MINUTES_PER_DAY = 24 * 60
        private const val MAX_DAILY_CLASSES = 2
    }
}
