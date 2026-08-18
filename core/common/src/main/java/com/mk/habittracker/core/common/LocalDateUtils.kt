package com.mk.habittracker.core.common

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data object LocalDateUtils {
    fun previousSevenDaysLabels(
        locale: Locale,
        today: LocalDate = LocalDate.now(),
    ): Array<String> = List(DAYS_IN_WEEK) { i ->
        val day = today.minusDays(DAYS_IN_WEEK - (i + 1L))
        day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale)
    }.toTypedArray()
}
