package com.mk.habittracker.data.sync

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mk.habittracker.data.dao.HabitDao
import com.mk.habittracker.data.model.CheckIn
import com.mk.habittracker.data.model.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
        return habitDao.getHabits(userId)
    }

    private fun pullHabits(userId: String) {
        scope.launch {
            try {
                val result = db.collection("habits")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()
                
                val habits = result.documents.map { Habit.from(id = it.id, data = it.data) }
                // // TODO: handle conflicts by comparing timestamps (e.g., updated_at)
                habitDao.addHabits(habits)
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pulling habits", e)
            }
        }
    }

    suspend fun addHabit(habit: Habit) {
        // Source of truth: Room
        habitDao.addHabit(habit)
        
        // Sync to Firestore
        scope.launch {
            try {
                db.collection("habits")
                    .document(habit.id)
                    .set(habit.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing habit", e)
                // TODO: Handle retry logic for offline-to-online transitions
            }
        }
    }

    fun getCheckIns(habitId: String, userId: String): Flow<List<CheckIn>> {
        pullCheckIns(habitId, userId)
        return habitDao.getCheckIns(habitId, userId)
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
                // // TODO: handle conflicts by comparing timestamps (e.g., updated_at)
                habitDao.addCheckIns(checkIns)
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pulling check-ins", e)
            }
        }
    }

    suspend fun addCheckIn(checkIn: CheckIn) {
        // Source of truth: Room
        habitDao.addCheckIn(checkIn)
        
        // Sync to Firestore
        scope.launch {
            try {
                db.collection("check_ins")
                    .document(checkIn.id)
                    .set(checkIn.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing check-in", e)
                // TODO: Handle retry logic
            }
        }
    }

    suspend fun deleteCheckIn(habitId: String, date: LocalDate, userId: String) {
        // Source of truth: Room
        habitDao.deleteCheckIn(habitId, date.toString())
        
        // Sync to Firestore
        scope.launch {
            try {
                // Find the check-in in Firestore to delete it
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
                // TODO: Handle retry logic
            }
        }
    }
}
