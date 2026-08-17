package com.mk.habittracker.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mk.habittracker.core.database.HabitDao
import com.mk.habittracker.core.database.asEntity
import com.mk.habittracker.core.database.asExternalModel
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.model.HabitDetail
import com.mk.habittracker.core.model.HabitStats
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun getHabits(userId: String): Flow<List<Habit>> {
        pullHabits(userId)
        return habitDao.getHabits(userId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    fun getHabit(
        userId: String,
        habitId: String,
    ): Flow<Habit?> = habitDao.getHabit(userId, habitId).map { it.asExternalModel() }

    private fun pullHabits(userId: String) {
        scope.launch {
            try {
                val result =
                    db
                        .collection("habits")
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
                db
                    .collection("habits")
                    .document(habit.id)
                    .set(habit.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing habit", e)
            }
        }
    }

    fun getCheckIns(
        userId: String,
        habitId: String,
    ): Flow<List<CheckIn>> {
        pullCheckIns(habitId, userId)
        return habitDao.getCheckIns(habitId, userId).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    private fun pullCheckIns(
        habitId: String,
        userId: String,
    ) {
        scope.launch {
            try {
                val result =
                    db
                        .collection("check_ins")
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
                db
                    .collection("check_ins")
                    .document(checkIn.id)
                    .set(checkIn.toMap())
                    .await()
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error pushing check-in", e)
            }
        }
    }

    suspend fun deleteCheckIn(
        habitId: String,
        date: LocalDate,
        userId: String,
    ) {
        habitDao.deleteCheckIn(habitId, date.toString())

        scope.launch {
            try {
                val result =
                    db
                        .collection("check_ins")
                        .whereEqualTo("habit_id", habitId)
                        .whereEqualTo("completed_date", date.toString())
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                for (doc in result.documents) {
                    db
                        .collection("check_ins")
                        .document(doc.id)
                        .delete()
                        .await()
                }
            } catch (e: Exception) {
                Log.e("HabitRepository", "Error deleting check-in", e)
            }
        }
    }

    suspend fun hasCheckedInToday(habitId: String): Boolean =
        habitDao.getCheckInForToday(auth.uid.orEmpty(), habitId) != null

    suspend fun getCurrentStreak(habitId: String): Int {
        val streak = habitDao.getLatestStreak(habitId) ?: return 0
        val lastLogged = LocalDate.parse(streak.lastDate)
        return if (lastLogged == LocalDate.now()) streak.streakLength else 0
    }

    fun getHabitDetail(habitId: String): Flow<HabitDetail> {
        val userId = auth.uid.orEmpty()
        return combine(
            getHabit(userId, habitId),
            getCheckIns(userId, habitId)
        ) { habit, checkIns ->
            HabitDetail(
                habit = habit ?: error("null habit"),
                stats = checkIns.computeStats(),
            )
        }
    }

    private fun List<CheckIn>.computeStats(): HabitStats {
        val datesDesc = this
            .map { it.completedDate }
            .sortedDescending()

        if (datesDesc.isEmpty()) return HabitStats(0, 0, 0)

        val today = LocalDate.now()
        var currentStreak = 0
        // grace until EOD
        if (ChronoUnit.DAYS.between(datesDesc.first(), today) <= 1) {
            currentStreak = 1
            var i = 1
            while (i < datesDesc.size &&
                ChronoUnit.DAYS.between(datesDesc[i], datesDesc[0]) == i.toLong()) {
                currentStreak++; i++
            }
        }

        var running = 1
        var best = 1
        for (j in 1 until datesDesc.size) {
            running = if (ChronoUnit.DAYS.between(datesDesc[j], datesDesc[j - 1]) == 1L) {
                running + 1
            } else {
                1
            }
            best = maxOf(best, running)
        }

        return HabitStats(currentStreak, best, datesDesc.size)
    }
}
