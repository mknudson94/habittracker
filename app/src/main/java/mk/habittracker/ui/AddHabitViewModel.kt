package mk.habittracker.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mk.habittracker.data.dao.HabitDao
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val habitDao: HabitDao,
): ViewModel() {
    val name = TextFieldState()

    fun saveHabit() {

    }
}
