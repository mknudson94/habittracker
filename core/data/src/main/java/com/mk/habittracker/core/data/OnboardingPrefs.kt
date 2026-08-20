package com.mk.habittracker.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingPrefs @Inject constructor(
    val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val HAS_CREATED_FIRST_HABIT = booleanPreferencesKey("has_created_first_habit")
    }

    val hasCreatedFirstHabit: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[Keys.HAS_CREATED_FIRST_HABIT] ?: false }

    suspend fun setHasCreatedFirstHabit() {
        dataStore.edit { prefs -> prefs[Keys.HAS_CREATED_FIRST_HABIT] = true }
    }
}
