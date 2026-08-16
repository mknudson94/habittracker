package com.mk.habittracker.core.data

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.habittracker.core.database.HabitDao
import com.mk.habittracker.core.database.HabitEntity
import com.mk.habittracker.core.model.Habit
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepositoryTest {
    private val habitDao: HabitDao = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        repository = HabitRepository(habitDao, firestore, auth, testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getHabits returns habits from DAO`() =
        runTest {
            val userId = "user1"
            val habitEntities =
                listOf(
                    HabitEntity("1", userId, "Habit 1", 1000L, null),
                    HabitEntity("2", userId, "Habit 2", 2000L, null),
                )

            every { habitDao.getHabits(userId) } returns flowOf(habitEntities)

            repository.getHabits(userId).test {
                val habits = awaitItem()
                assertThat(habits).hasSize(2)
                assertThat(habits[0].id).isEqualTo("1")
                assertThat(habits[1].id).isEqualTo("2")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `addHabit calls DAO`() =
        runTest {
            val habit = Habit("1", "user1", "Habit 1", 1000L, null)

            repository.addHabit(habit)

            coVerify { habitDao.addHabit(match { it.id == "1" }) }
        }
}
