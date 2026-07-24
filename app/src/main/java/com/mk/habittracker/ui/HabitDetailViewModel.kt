package com.mk.habittracker.ui

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
import com.mk.habittracker.data.dao.HabitDao
import com.mk.habittracker.data.model.Habit
import com.mk.habittracker.data.sync.HabitRepository

@HiltViewModel(assistedFactory = HabitDetailViewModel.Factory::class)
class HabitDetailViewModel
    @AssistedInject
    constructor(
        @Assisted val habitId: String,
        private val repository: HabitRepository,
        private val habitDao: HabitDao, // Keep DAO for single habit lookup if repo doesn't have it
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
