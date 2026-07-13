package com.mk.habittracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mk.habittracker.data.dao.HabitDao
import com.mk.habittracker.data.dao.UserDao
import com.mk.habittracker.data.model.CheckIn
import com.mk.habittracker.data.model.Habit
import com.mk.habittracker.data.model.User
import com.mk.habittracker.data.util.Converters

@Database(
    entities = [
        User::class,
        Habit::class,
        CheckIn::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun habitDao(): HabitDao
}
