package com.mk.habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.mk.habittracker.data.dao.HabitDao
import com.mk.habittracker.data.model.CheckIn
import com.mk.habittracker.data.model.Habit
import java.time.LocalDate
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val DEFAULT_STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel
class MainScreenViewModel
    @Inject
    constructor(
        val habitDao: HabitDao,
    ) : ViewModel() {
        val habits: StateFlow<ImmutableList<Habit>> =
            habitDao
                .getHabits(userId = "1")
                .map { it.toImmutableList() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
                    initialValue = persistentListOf(),
                )

        fun getCheckIns(habitId: String): StateFlow<ImmutableList<CheckIn>> =
            habitDao
                .getCheckIns(habitId)
                .map { it.toImmutableList() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
                    initialValue = persistentListOf(),
                )

        fun toggleCheckIn(
            isChecked: Boolean,
            habitId: String,
        ) {
            viewModelScope.launch {
                if (isChecked) {
                    habitDao.addCheckIn(
                        CheckIn(
                            id = Uuid.random().toString(),
                            habitId = habitId,
                            completedDate = LocalDate.now(),
                        ),
                    )
                } else {
                    habitDao.deleteCheckIn(habitId, LocalDate.now().toString())
                }
            }
        }
    }
