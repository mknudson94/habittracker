package com.mk.habittracker.feature.addhabit

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
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
    var tagId by mutableStateOf<ByteArray?>(null)
        private set

        private val userId: String
            get() = Firebase.auth.currentUser?.uid ?: "anonymous"

    fun onTagPaired(id: ByteArray) {
        tagId = id
    }

    suspend fun saveHabit() {
        repository.addHabit(
            Habit(
                id = habitId,
                userId = userId,
                name = name.text.toString(),
                createdAt =
                    java.time.Instant
                        .now()
                        .toEpochMilli(),
                tagId = tagId,
            ),
        )
        }
    }
