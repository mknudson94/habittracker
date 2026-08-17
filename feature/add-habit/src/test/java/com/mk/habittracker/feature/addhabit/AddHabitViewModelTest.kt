package com.mk.habittracker.feature.addhabit

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.core.data.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
@RunWith(RobolectricTestRunner::class)
class AddHabitViewModelTest {

    private val repository: HabitRepository = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()

    private lateinit var viewModel: AddHabitViewModel

    @Before
    fun setup() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "test-user-id"

        viewModel = AddHabitViewModel(repository, firebaseAuth)
    }

    @Test
    fun `initial state has random habitId`() {
        assertThat(viewModel.habitId).isNotEmpty()
    }

    @Test
    fun `initial state has empty name`() {
        assertThat(viewModel.name.text.toString()).isEmpty()
    }

    @Test
    fun `initial state has null tagId`() {
        assertThat(viewModel.tagId).isNull()
    }

    @Test
    fun `onTagPaired updates tagId`() {
        val tagId = byteArrayOf(1, 2, 3)
        viewModel.onTagPaired(tagId)
        assertThat(viewModel.tagId).isEqualTo(tagId)
    }

    @Test
    fun `saveHabit calls repository with correct data`() = runTest {
        val habitName = "Exercise"
        viewModel.name.setTextAndPlaceCursorAtEnd(habitName)
        val tagId = byteArrayOf(4, 5, 6)
        viewModel.onTagPaired(tagId)

        viewModel.saveHabit()

        coVerify {
            repository.addHabit(
                match { habit ->
                    habit.name == habitName &&
                    habit.userId == "test-user-id" &&
                    habit.tagId?.contentEquals(tagId) == true &&
                    habit.id == viewModel.habitId
                }
            )
        }
    }

    @Test
    fun `habitId is stable across multiple operations`() {
        val initialId = viewModel.habitId
        viewModel.onTagPaired(byteArrayOf(1))
        viewModel.name.setTextAndPlaceCursorAtEnd("Test")
        assertThat(viewModel.habitId).isEqualTo(initialId)
    }

    @Test
    fun `onTagPaired can be called multiple times, keeping the last value`() {
        val tag1 = byteArrayOf(1)
        val tag2 = byteArrayOf(2)
        
        viewModel.onTagPaired(tag1)
        assertThat(viewModel.tagId).isEqualTo(tag1)
        
        viewModel.onTagPaired(tag2)
        assertThat(viewModel.tagId).isEqualTo(tag2)
    }

    @Test
    fun `saveHabit uses anonymous userId when user is not logged in`() = runTest {
        every { firebaseAuth.currentUser } returns null

        viewModel.saveHabit()

        coVerify {
            repository.addHabit(
                match { habit ->
                    habit.userId == "anonymous"
                }
            )
        }
    }

    @Test
    fun `saveHabit uses current timestamp`() = runTest {
        val before = System.currentTimeMillis()
        viewModel.saveHabit()
        val after = System.currentTimeMillis()

        coVerify {
            repository.addHabit(
                match { habit ->
                    habit.createdAt in before..after
                }
            )
        }
    }

    @Test(expected = Exception::class)
    fun `saveHabit propagates repository exceptions`() = runTest {
        coEvery { repository.addHabit(match { true }) } throws Exception("Database error")
        viewModel.saveHabit()
    }
}
