package com.mk.habittracker.data.sync

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Source
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
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val db = Firebase.firestore

    fun getHabits(userId: String): Flow<List<Habit>> {
        updateHabits(userId)
        return habitDao.getHabits(userId)
    }

    private fun updateHabits(userId: String) {
        try {
            scope.launch(Dispatchers.IO) {
                val result = db.collection("habits")
                    .get(Source.SERVER)
                    .await()
                habitDao.addHabits(result.documents.map { Habit.from(it.data) })
            }
        } catch (e: ExecutionException) {
            Log.e("firestore", e.cause.toString())
        }
    }

    fun getCheckIns(habitId: String): Flow<List<CheckIn>> {
        updateHabits(habitId)
        return habitDao.getCheckIns(habitId)
    }

    private fun updateCheckIns(habitId: String) {
        try {
            scope.launch(Dispatchers.IO) {
                val result = db.collection("habits")
                    .document(habitId)
                    .collection("check_ins")
                    .get(Source.SERVER)
                    .await()
                habitDao.addCheckIns(result.documents.map { CheckIn.from(it.data) })
            }
        } catch (e: ExecutionException) {
            Log.e("firestore", e.cause.toString())
        }
    }
}
