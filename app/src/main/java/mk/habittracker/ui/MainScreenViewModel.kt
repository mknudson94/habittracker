package mk.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import mk.habittracker.data.dao.HabitDao
import mk.habittracker.data.model.CheckIn
import mk.habittracker.data.model.Habit
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    val habitDao: HabitDao
): ViewModel() {

    val habits: StateFlow<List<Habit>> = habitDao.getHabits(userId = "1").stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getCheckIns(habitId: String) = habitDao.getCheckIns(habitId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addHabit(habitName: String) {
        habitDao.addHabit(Habit(
            id = "1",
            userId = "1",
            name = habitName,
            createdAt = Instant.now().toEpochMilli()
        ))
    }
}
