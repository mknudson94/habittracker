package com.mk.habittracker.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mk.habittracker.core.database.HabitDao
import com.mk.habittracker.core.database.asEntity
import com.mk.habittracker.core.database.asExternalModel
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.model.HabitDetail
import com.mk.habittracker.core.model.computeStats
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
        if (auth.currentUser?.uid != userId) return
        scope.launch {
            try {
                val result =
                    db
                        .collection("habits")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                val habits = result.documents.map { it.data!!.toHabit(id = it.id) }
                habitDao.addHabits(habits.map { it.asEntity() })
            } catch (e: FirebaseFirestoreException) {
                Log.e("HabitRepository", "Error pulling habits", e)
            }
        }
    }

    suspend fun addHabit(habit: Habit) {
        habitDao.addHabit(habit.asEntity())

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != habit.userId) {
            Log.w(
                "HabitRepository",
                "Skipping Fstore sync: auth UID $currentUserId != habit UID ${habit.userId}",
            )
            return
        }

        scope.launch {
            try {
                db
                    .collection("habits")
                    .document(habit.id)
                    .set(habit.toMap())
                    .await()
            } catch (e: FirebaseFirestoreException) {
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
        if (auth.currentUser?.uid != userId) return
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
            } catch (e: FirebaseFirestoreException) {
                Log.e("HabitRepository", "Error pulling check-ins", e)
            }
        }
    }

    suspend fun addCheckIn(checkIn: CheckIn) {
        habitDao.addCheckIn(checkIn.asEntity())

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != checkIn.userId) {
            Log.w(
                "HabitRepository",
                "Skipping Firestore sync: user auth $currentUserId != check-in ${checkIn.userId}",
            )
            return
        }

        scope.launch {
            try {
                db
                    .collection("check_ins")
                    .document(checkIn.id)
                    .set(checkIn.toMap())
                    .await()
            } catch (e: FirebaseFirestoreException) {
                Log.e("HabitRepository", "Error pushing check-in", e)
            }
        }
    }

    suspend fun deleteCheckIn(
        habitId: String,
        date: LocalDate,
        userId: String,
    ) {
        habitDao.deleteCheckIn(userId, habitId, date.toString())

        if (auth.currentUser?.uid != userId) return

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
            } catch (e: FirebaseFirestoreException) {
                Log.e("HabitRepository", "Error deleting check-in", e)
            }
        }
    }

    suspend fun hasCheckedInToday(habitId: String): Boolean =
        habitDao.getCheckInForToday(auth.uid.orEmpty(), habitId) != null

    suspend fun getCurrentStreak(
        userId: String,
        habitId: String,
    ): Int {
        val streak = habitDao.getLatestStreak(userId, habitId) ?: return 0
        val lastLogged = LocalDate.parse(streak.lastDate)
        return if (lastLogged == LocalDate.now()) streak.streakLength else 0
    }

    fun getHabitDetail(habitId: String): Flow<HabitDetail> {
        val userId = auth.uid.orEmpty()
        return combine(
            getHabit(userId, habitId),
            getCheckIns(userId, habitId),
        ) { habit, checkIns ->
            HabitDetail(
                habit = habit ?: error("null habit"),
                stats = checkIns.computeStats(),
            )
        }
    }
}

internal fun Habit.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "user_id" to userId,
    "name" to name,
    "created_at" to createdAt,
    "tag_id" to tagId?.let { Blob.fromBytes(it) },
)

internal fun Map<String, Any?>.toHabit(id: String) = Habit(
    id = id,
    userId = get("user_id") as? String ?: error("couldn't read user_id"),
    name = get("name") as? String ?: error("couldn't read name"),
    createdAt = get("created_at") as? Long ?: error("couldn't read created_at"),
    tagId = (get("tag_id") as? Blob)?.toBytes(),
)
