package com.mk.habittracker.feature.addhabit

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.data.HabitRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel
class AddHabitViewModel
    @Inject
    constructor(
        private val repository: HabitRepository,
    ) : ViewModel() {
        val habitId = Uuid.random().toString()
        val name = TextFieldState()

        private val userId: String
            get() = Firebase.auth.currentUser?.uid ?: "anonymous"

        fun saveHabit() {
            viewModelScope.launch {
                repository.addHabit(
                    Habit(
                        id = habitId,
                        userId = userId,
                        name = name.text.toString(),
                        createdAt =
                            java.time.Instant
                                .now()
                                .toEpochMilli(),
                    ),
                )
            }
        }
    }
