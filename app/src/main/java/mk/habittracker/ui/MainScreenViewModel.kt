package mk.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mk.habittracker.data.dao.HabitDao
import mk.habittracker.data.model.CheckIn
import mk.habittracker.data.model.Habit
import java.time.LocalDate
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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

    fun toggleCheckIn(isChecked: Boolean, habitId: String) {
        viewModelScope.launch {
            if (isChecked) {
                habitDao.addCheckIn(
                    CheckIn(
                        id = Uuid.random().toString(),
                        habitId = habitId,
                        completedDate = LocalDate.now(),
                    )
                )
            }
            else {
                habitDao.deleteCheckIn(habitId, LocalDate.now().toString())
            }
        }
    }
}
