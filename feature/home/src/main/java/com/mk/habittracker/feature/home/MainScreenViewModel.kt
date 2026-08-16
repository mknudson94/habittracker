package com.mk.habittracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
        private val repository: HabitRepository,
    ) : ViewModel() {
        private val userId: String
            get() = Firebase.auth.currentUser?.uid ?: "anonymous"

        val habits: StateFlow<ImmutableList<Habit>> =
            repository
                .getHabits(userId = userId)
                .map { it.toImmutableList() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
                    initialValue = persistentListOf(),
                )

        fun getCheckIns(habitId: String): StateFlow<List<CheckIn>> =
            repository
                .getCheckIns(habitId, userId)
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
                    repository.addCheckIn(
                        CheckIn(
                            id = Uuid.random().toString(),
                            habitId = habitId,
                            completedDate = LocalDate.now(),
                            userId = userId,
                        ),
                    )
                } else {
                    repository.deleteCheckIn(habitId, LocalDate.now(), userId)
                }
            }
        }
    }
