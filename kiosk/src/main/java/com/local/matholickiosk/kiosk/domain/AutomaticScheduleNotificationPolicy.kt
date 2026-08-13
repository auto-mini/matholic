package com.local.matholickiosk.kiosk.domain

data class AutomaticScheduleProblemNotificationState(
    val problemKey: String,
    val lastNotifiedAtElapsedMs: Long,
)

data class AutomaticScheduleProblemNotificationDecision(
    val notify: Boolean,
    val state: AutomaticScheduleProblemNotificationState,
)

object AutomaticScheduleNotificationPolicy {
    const val REPEAT_INTERVAL_MS = 10 * 60_000L

    fun onProblem(
        problemKey: String,
        nowElapsedMs: Long,
        previous: AutomaticScheduleProblemNotificationState?,
    ): AutomaticScheduleProblemNotificationDecision {
        require(problemKey.isNotBlank()) { "Automatic schedule problem key is blank" }
        require(nowElapsedMs >= 0L) { "Elapsed time must not be negative" }
        val shouldNotify = previous == null ||
            previous.problemKey != problemKey ||
            nowElapsedMs - previous.lastNotifiedAtElapsedMs >= REPEAT_INTERVAL_MS
        return AutomaticScheduleProblemNotificationDecision(
            notify = shouldNotify,
            state = if (shouldNotify) {
                AutomaticScheduleProblemNotificationState(problemKey, nowElapsedMs)
            } else {
                previous
            },
        )
    }
}
