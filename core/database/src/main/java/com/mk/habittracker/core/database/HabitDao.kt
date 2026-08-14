package com.mk.habittracker.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHabit(habit: HabitEntity)

    @Query(
        "UPDATE habit SET tag_id = :tagId WHERE user_id = :userId AND id = :habitId"
    )
    suspend fun updateHabitTagId(userId: String, habitId: String, tagId: ByteArray)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHabits(habits: List<HabitEntity>)

    @Query(
        "SELECT * FROM habit " +
            "WHERE :userId = habit.user_id " +
            "ORDER BY habit.name DESC"
    )
    fun getHabits(userId: String): Flow<List<HabitEntity>>

    @Query(
        "SELECT * FROM habit " +
            "WHERE :userId = habit.user_id " +
            "AND :habitId = habit.id",
    )
    fun getHabit(
        userId: String,
        habitId: String,
    ): Flow<HabitEntity>

    @Query(
        "SELECT * FROM check_in " +
            "WHERE :habitId = check_in.habit_id " +
            "AND :userId = check_in.user_id " +
            "ORDER BY julianday(check_in.completed_date) DESC",
    )
    fun getCheckIns(habitId: String, userId: String): Flow<List<CheckInEntity>>

    @Query(
        "SELECT * FROM check_in " +
            "WHERE :habitId = check_in.habit_id " +
            "AND :userId = check_in.user_id " +
            "AND date('now') = date(check_in.completed_date)",
    )
    suspend fun getCheckInForToday(userId: String, habitId: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCheckIn(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCheckIns(checkIns: List<CheckInEntity>)

    @Query(
        "DELETE FROM check_in " +
            "WHERE completed_date = :date " +
            "AND habit_id = :habitId",
    )
    suspend fun deleteCheckIn(
        habitId: String,
        date: String,
    )

    @Query(
        "WITH " +
            "deduped AS (SELECT DISTINCT completed_date FROM check_in WHERE habit_id = :habitId), " +
            "dated_rows AS (SELECT completed_date, " +
            "CAST(julianday(completed_date) AS INTEGER) - ROW_NUMBER() OVER (ORDER BY completed_date) AS island " +
            "FROM deduped), " +
            "streak_groups AS (SELECT MAX(completed_date) AS lastDate, COUNT(*) AS streakLength " +
            "FROM dated_rows GROUP BY island) " +
            "SELECT lastDate, streakLength FROM streak_groups " +
            "WHERE lastDate = (SELECT MAX(lastDate) FROM streak_groups)"
    )
    suspend fun getLatestStreak(habitId: String): StreakEntity?
}
