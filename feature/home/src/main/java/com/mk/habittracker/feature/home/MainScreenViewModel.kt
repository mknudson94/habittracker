package com.mk.habittracker.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.data.OnboardingPrefs
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val DEFAULT_STOP_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val auth: FirebaseAuth,
    onboardingPrefs: OnboardingPrefs,
) : ViewModel() {
    private val userId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    private val authState = MutableStateFlow(auth.currentUser)

    init {
        auth.addAuthStateListener {
            authState.value = it.currentUser
        }
    }

    val habits: StateFlow<ImmutableList<Habit>> = authState
        .flatMapLatest { user ->
            repository.getHabits(userId = user?.uid ?: "anonymous")
        }.map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
            initialValue = persistentListOf(),
        )

    val shouldShowTooltip: StateFlow<Boolean> = combine(
        habits,
        onboardingPrefs.hasCreatedFirstHabit,
    ) { currentHabits, hasCreatedHabit ->
        val hasHabits = currentHabits.isNotEmpty()
        val shouldShow = !hasHabits && !hasCreatedHabit
        Log.d("mknudson", "shouldShowTooltip -> $shouldShow")
        shouldShow
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_STOP_TIMEOUT_MILLIS),
        initialValue = false,
    )

    fun getCheckIns(habitId: String): StateFlow<List<CheckIn>> = repository
        .getCheckIns(userId, habitId)
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
