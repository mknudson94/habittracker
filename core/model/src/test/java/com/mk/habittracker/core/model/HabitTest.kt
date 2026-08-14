package com.mk.habittracker.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HabitTest {

    @Test
    fun `Habit toMap returns expected map`() {
        val tagId = byteArrayOf(1, 2, 3)
        val habit = Habit(
            id = "habit1",
            userId = "user1",
            name = "Exercise",
            createdAt = 123456789L,
            tagId = tagId
        )

        val map = habit.toMap()

        assertThat(map["id"]).isEqualTo("habit1")
        assertThat(map["user_id"]).isEqualTo("user1")
        assertThat(map["name"]).isEqualTo("Exercise")
        assertThat(map["created_at"]).isEqualTo(123456789L)
        assertThat(map["tag_id"]).isEqualTo(tagId)
    }

    @Test
    fun `Habit from returns expected Habit object`() {
        val tagId = byteArrayOf(4, 5, 6)
        val data = mapOf(
            "user_id" to "user2",
            "name" to "Read",
            "created_at" to 987654321L,
            "tag_id" to tagId
        )

        val habit = Habit.from("habit2", data)

        assertThat(habit.id).isEqualTo("habit2")
        assertThat(habit.userId).isEqualTo("user2")
        assertThat(habit.name).isEqualTo("Read")
        assertThat(habit.createdAt).isEqualTo(987654321L)
        assertThat(habit.tagId).isEqualTo(tagId)
    }

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
