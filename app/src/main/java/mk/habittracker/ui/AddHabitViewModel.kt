package mk.habittracker.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mk.habittracker.data.dao.HabitDao
import mk.habittracker.data.model.Habit
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val habitDao: HabitDao,
): ViewModel() {
    val habitId = Uuid.random().toString()
    val name = TextFieldState()

    fun saveHabit() {
        habitDao.addHabit(
            Habit(
                id = habitId,
                userId = "1",
                name = name.text.toString(),
                createdAt = java.time.Instant.now().toEpochMilli()
            )
        )
    }
}
