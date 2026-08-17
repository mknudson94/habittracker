package com.mk.habittracker.feature.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.HabitDetail
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = HabitDetailViewModel.Factory::class)
class HabitDetailViewModel @AssistedInject constructor(
    @Assisted val habitId: String,
    repository: HabitRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(habitId: String): HabitDetailViewModel
    }

    private val userId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    val habitDetail: StateFlow<HabitDetail?> =
        repository
            .getHabitDetail(habitId = habitId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val checkIns =
        repository.getCheckIns(userId, habitId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
}
