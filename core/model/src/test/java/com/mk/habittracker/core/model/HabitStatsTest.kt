package com.mk.habittracker.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class HabitStatsTest {
    @Test
    fun `computeStats returns empty stats for empty check-ins`() {
        val checkIns = emptyList<CheckIn>()
        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(0)
        assertThat(stats.bestStreak).isEqualTo(0)
        assertThat(stats.totalCheckIns).isEqualTo(0)
    }

    @Test
    fun `computeStats calculates streak correctly for consecutive days`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn(today),
            checkIn(today.minusDays(1)),
            checkIn(today.minusDays(2)),
        )

        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(3)
        assertThat(stats.bestStreak).isEqualTo(3)
        assertThat(stats.totalCheckIns).isEqualTo(3)
    }

    @Test
    fun `computeStats handles gap in streak`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn(today),
            checkIn(today.minusDays(1)),
            // Gap at today.minusDays(2)
            checkIn(today.minusDays(3)),
        )

        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(2)
        assertThat(stats.bestStreak).isEqualTo(2)
        assertThat(stats.totalCheckIns).isEqualTo(3)
    }

    @Test
    fun `computeStats calculates best streak correctly`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            // Current streak 2
            checkIn(today),
            checkIn(today.minusDays(1)),
            // Gap
            // Best streak 4
            checkIn(today.minusDays(4)),
            checkIn(today.minusDays(5)),
            checkIn(today.minusDays(6)),
            checkIn(today.minusDays(7)),
        )

        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(2)
        assertThat(stats.bestStreak).isEqualTo(4)
    }

    @Test
    fun `computeStats handles check-in yesterday but not today as current streak active`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn(today.minusDays(1)),
            checkIn(today.minusDays(2)),
        )

        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(2)
    }

    @Test
    fun `computeStats handles check-in two days ago as current streak broken`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn(today.minusDays(2)),
            checkIn(today.minusDays(3)),
        )

        val stats = checkIns.computeStats()

        assertThat(stats.currentStreak).isEqualTo(0)
        assertThat(stats.bestStreak).isEqualTo(2)
    }

    private fun checkIn(date: LocalDate) = CheckIn(
        id = date.toString(),
        habitId = "h1",
        userId = "u1",
        completedDate = date,
    )
}
