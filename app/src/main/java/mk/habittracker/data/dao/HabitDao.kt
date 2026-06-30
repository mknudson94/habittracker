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
    fun addHabit(habit: Habit)

    @Query(
        "SELECT * FROM check_in " +
            "WHERE :habitId = check_in.habit_id " +
            "AND julianday('now') - julianday(check_in.completed_date) <= 7"
    )
    fun getCheckIns(habitId: Int): Flow<List<CheckIn>>

    @Query("SELECT * FROM habit WHERE :userId = habit.user_id")
    fun getHabits(userId: Int): Flow<List<Habit>>
}