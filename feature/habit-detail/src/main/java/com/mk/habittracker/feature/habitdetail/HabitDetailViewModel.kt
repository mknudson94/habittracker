package com.mk.habittracker.feature.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.mk.habittracker.core.database.HabitDao
import com.mk.habittracker.core.database.asExternalModel
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.data.HabitRepository

@HiltViewModel(assistedFactory = HabitDetailViewModel.Factory::class)
class HabitDetailViewModel
    @AssistedInject
    constructor(
        @Assisted val habitId: String,
        private val repository: HabitRepository,
        private val habitDao: HabitDao,
    ) : ViewModel() {

        @AssistedFactory
        interface Factory {
            fun create(habitId: String): HabitDetailViewModel
        }

        private val userId: String
            get() = Firebase.auth.currentUser?.uid ?: "anonymous"

        val habit: StateFlow<Habit?> =
            habitDao
                .getHabit(userId, habitId)
                .map { it?.asExternalModel() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        val nCheckIns =
            repository.getCheckIns(habitId, userId).map { it.size.toString() }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "0",
            )
    }
