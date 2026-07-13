package com.mk.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.mk.habittracker.data.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(user: User)
}
