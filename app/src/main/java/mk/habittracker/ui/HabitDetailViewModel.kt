package mk.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mk.habittracker.data.dao.HabitDao

@HiltViewModel(assistedFactory = HabitDetailViewModel.Factory::class)
class HabitDetailViewModel @AssistedInject constructor(
    @Assisted val habitId: Int,
    val habitDao: HabitDao,
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(habitId: Int): HabitDetailViewModel
    }

    val habit = habitDao.getHabit(userId = 1, habitId = habitId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val nCheckIns = habitDao.getCheckIns(habitId).map { it.size.toString() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "?"
    )
}