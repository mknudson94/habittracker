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

    @Query("SELECT * FROM habit WHERE :userId = habit.user_id")
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
            "AND julianday('now') - julianday(check_in.completed_date) <= 7",
    )
    fun getCheckIns(habitId: String, userId: String): Flow<List<CheckInEntity>>

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
}
