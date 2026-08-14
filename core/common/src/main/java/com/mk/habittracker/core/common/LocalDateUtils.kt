package com.mk.habittracker.core.common

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data object LocalDateUtils {
    fun previousSevenDaysLabels(
        locale: Locale,
        clock: LocalDate = LocalDate.now()
    ): Array<String> {
        val today = clock
        val labels = arrayOfNulls<String>(DAYS_IN_WEEK)
        repeat(DAYS_IN_WEEK) { i ->
            val day = today.minusDays(DAYS_IN_WEEK - (i + 1L))
            labels[i] = day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale)
        }
        return labels as Array<String>
    }
}
