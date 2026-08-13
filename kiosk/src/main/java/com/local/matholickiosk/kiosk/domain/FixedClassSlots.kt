package com.local.matholickiosk.kiosk.domain

object FixedClassSlots {
    val names: List<String> = listOf(
        "월1", "월2",
        "화1", "화2",
        "수1", "수2",
        "목1", "목2",
        "금1", "금2",
        "토1", "토2",
    )

    fun contains(name: String): Boolean = name in names

    fun isoDayOfWeek(name: String): Int? = when (name.firstOrNull()) {
        '월' -> 1
        '화' -> 2
        '수' -> 3
        '목' -> 4
        '금' -> 5
        '토' -> 6
        else -> null
    }
}
