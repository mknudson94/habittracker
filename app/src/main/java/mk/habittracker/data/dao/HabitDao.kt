package mk.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import mk.habittracker.data.model.CheckIn
import mk.habittracker.data.model.Habit

@Dao
interface HabitDao {
    @Insert
    fun addHabit(habit: Habit)

    @Query(
        "SELECT * FROM habit " +
        "JOIN check_in ON check_in.habit_id = habit.id " +
        "WHERE julianday('now') - julianday(check_in.completed_date) <= 7"
    )
    fun getHabitsList(): Map<Habit, List<CheckIn>>
}