package mk.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mk.habittracker.data.model.CheckIn
import mk.habittracker.data.model.Habit

@Dao
interface HabitDao {
    @Insert
    suspend fun addHabit(habit: Habit)

    @Query("SELECT * FROM habit WHERE :userId = habit.user_id")
    fun getHabits(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habit " +
            "WHERE :userId = habit.user_id " +
            "AND :habitId = habit.id"
    )
    fun getHabit(userId: String, habitId: String): Flow<Habit>

    @Query(
        "SELECT * FROM check_in " +
            "WHERE :habitId = check_in.habit_id " +
            "AND julianday('now') - julianday(check_in.completed_date) <= 7"
    )
    fun getCheckIns(habitId: String): Flow<List<CheckIn>>

    @Insert
    suspend fun addCheckIn(checkIn: CheckIn)

    @Query(
        "DELETE FROM check_in " +
                "WHERE completed_date = :date " +
                "AND habit_id = :habitId"
    )
    suspend fun deleteCheckIn(habitId: String, date: String)
}
