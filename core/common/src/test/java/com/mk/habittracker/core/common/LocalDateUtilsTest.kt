package com.mk.habittracker.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class LocalDateUtilsTest {
    @Test
    fun `previousSevenDaysLabels returns correct labels for Monday in US locale`() {
        // Monday, May 19, 2025
        val monday = LocalDate.of(2025, 5, 19)
        val labels = LocalDateUtils.previousSevenDaysLabels(Locale.US, monday)

        // Previous 7 days including today (i.e. T, W, T, F, S, S, M)
        // Wait, the logic is: today.minusDays(DAYS_IN_WEEK - (i + 1L))
        // i=0: today - 6 = Tuesday
        // i=1: today - 5 = Wednesday
        // i=2: today - 4 = Thursday
        // i=3: today - 3 = Friday
        // i=4: today - 2 = Saturday
        // i=5: today - 1 = Sunday
        // i=6: today - 0 = Monday

        assertThat(labels).hasLength(7)
        assertThat(labels).isEqualTo(arrayOf("T", "W", "T", "F", "S", "S", "M"))
    }

    @Test
    fun `previousSevenDaysLabels returns correct labels for Sunday in US locale`() {
        // Sunday, May 18, 2025
        val sunday = LocalDate.of(2025, 5, 18)
        val labels = LocalDateUtils.previousSevenDaysLabels(Locale.US, sunday)

        // i=0: today - 6 = Monday
        // i=1: today - 5 = Tuesday
        // i=2: today - 4 = Wednesday
        // i=3: today - 3 = Thursday
        // i=4: today - 2 = Friday
        // i=5: today - 1 = Saturday
        // i=6: today - 0 = Sunday

        assertThat(labels).isEqualTo(arrayOf("M", "T", "W", "T", "F", "S", "S"))
    }
}
