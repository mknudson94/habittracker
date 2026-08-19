package com.mk.habittracker.core.data

import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.Blob
import com.mk.habittracker.core.model.Habit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class HabitMappingTest {
    @Before
    fun setUp() {
        mockkStatic(Blob::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Habit toMap returns expected map with Blob for tagId`() {
        val tagBytes = byteArrayOf(1, 2, 3)
        val mockBlob = mockk<Blob>()
        every { Blob.fromBytes(tagBytes) } returns mockBlob

        val habit = Habit(
            id = "habit1",
            userId = "user1",
            name = "Exercise",
            createdAt = 123456789L,
            tagId = tagBytes,
        )

        val map = habit.toMap()

        assertThat(map["id"]).isEqualTo("habit1")
        assertThat(map["user_id"]).isEqualTo("user1")
        assertThat(map["name"]).isEqualTo("Exercise")
        assertThat(map["created_at"]).isEqualTo(123456789L)
        assertThat(map["tag_id"]).isEqualTo(mockBlob)
    }

    @Test
    fun `toHabit returns expected Habit object from map with Blob`() {
        val tagBytes = byteArrayOf(4, 5, 6)
        val mockBlob = mockk<Blob>()
        every { mockBlob.toBytes() } returns tagBytes

        val data = mapOf(
            "user_id" to "user2",
            "name" to "Read",
            "created_at" to 987654321L,
            "tag_id" to mockBlob,
        )

        val habit = data.toHabit("habit2")

        assertThat(habit.id).isEqualTo("habit2")
        assertThat(habit.userId).isEqualTo("user2")
        assertThat(habit.name).isEqualTo("Read")
        assertThat(habit.createdAt).isEqualTo(987654321L)
        assertThat(habit.tagId).isEqualTo(tagBytes)
    }
}
