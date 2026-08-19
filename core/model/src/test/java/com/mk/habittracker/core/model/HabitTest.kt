package com.mk.habittracker.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HabitTest {
    @Test
    fun `Habit equality works correctly with tagId`() {
        val tagId1 = byteArrayOf(1, 2)
        val tagId2 = byteArrayOf(1, 2)
        val tagId3 = byteArrayOf(1, 3)

        val habit1 = Habit("h1", "u1", "n1", 100L, tagId1)
        val habit2 = Habit("h1", "u1", "n1", 100L, tagId2)
        val habit3 = Habit("h1", "u1", "n1", 100L, tagId3)
        val habit4 = Habit("h1", "u1", "n1", 100L, null)

        assertThat(habit1).isEqualTo(habit2)
        assertThat(habit1).isNotEqualTo(habit3)
        assertThat(habit1).isNotEqualTo(habit4)
    }
}
