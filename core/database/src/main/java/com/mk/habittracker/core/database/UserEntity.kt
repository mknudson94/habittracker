package com.mk.habittracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mk.habittracker.core.model.User
import java.time.ZoneId

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "user_timezone") val userTimezone: ZoneId,
)

fun UserEntity.asExternalModel() = User(
    id = id,
    email = email,
    createdAt = createdAt,
    userTimezone = userTimezone
)

fun User.asEntity() = UserEntity(
    id = id,
    email = email,
    createdAt = createdAt,
    userTimezone = userTimezone
)
