package com.mk.habittracker.core.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.mk.habittracker.core.database.HabitDao
import com.mk.habittracker.core.database.asEntity
import com.mk.habittracker.core.database.asExternalModel
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = Firebase.firestore

    fun getHabits(userId: String): Flow<List<Habit>> {
        pullHabits(userId)
        return habitDao.getHabits(userId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    fun getHabit(userId: String, habitId: String): Flow<Habit?> =
        habitDao.getHabit(userId, habitId).map { it?.asExternalModel() }

    private fun pullHabits(userId: String) {
        scope.launch {
            try {
                val result = db.collection("habits")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()
                
                val habits = result.documents.map { Habit.from(id = it.id, data = it.data) }
                habitDao.addHabits(habits.map { it.asEntity() })
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pulling habits", e)
            }
        }
    }

    suspend fun addHabit(habit: Habit) {
        habitDao.addHabit(habit.asEntity())
        
        scope.launch {
            try {
                db.collection("habits")
                    .document(habit.id)
                    .set(habit.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing habit", e)
            }
        }
    }

    fun getCheckIns(habitId: String, userId: String): Flow<List<CheckIn>> {
        pullCheckIns(habitId, userId)
        return habitDao.getCheckIns(habitId, userId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    private fun pullCheckIns(habitId: String, userId: String) {
        scope.launch {
            try {
                val result = db.collection("check_ins")
                    .whereEqualTo("habit_id", habitId)
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()
                
                val checkIns = result.documents.map { CheckIn.from(id = it.id, data = it.data) }
                habitDao.addCheckIns(checkIns.map { it.asEntity() })
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pulling check-ins", e)
            }
        }
    }

    suspend fun addCheckIn(checkIn: CheckIn) {
        habitDao.addCheckIn(checkIn.asEntity())
        
        scope.launch {
            try {
                db.collection("check_ins")
                    .document(checkIn.id)
                    .set(checkIn.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing check-in", e)
            }
        }
    }

    suspend fun deleteCheckIn(habitId: String, date: LocalDate, userId: String) {
        habitDao.deleteCheckIn(habitId, date.toString())
        
        scope.launch {
            try {
                val result = db.collection("check_ins")
                    .whereEqualTo("habit_id", habitId)
                    .whereEqualTo("completed_date", date.toString())
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()
                
                for (doc in result.documents) {
                    db.collection("check_ins").document(doc.id).delete().await()
                }
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error deleting check-in", e)
            }
        }
    }

    suspend fun hasCheckedInToday(habitId: String): Boolean =
        habitDao.getCheckInForToday(Firebase.auth.uid.orEmpty(), habitId) != null

    suspend fun getCurrentStreak(habitId: String): Int {
        val streak = habitDao.getLatestStreak(habitId) ?: return 0
        val lastLogged = LocalDate.parse(streak.lastDate)
        return if (lastLogged == LocalDate.now()) streak.streakLength else 0
    }
}
