package com.mk.habittracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "user_id" to userId,
        "name" to name,
        "created_at" to createdAt,
    )

    companion object {
        fun from(data: Map<String, Any>?): Habit {
            data!!
            return Habit(
                id = data["id"] as? String ?: error("couldn't read id"),
                userId = data["user_id"] as? String ?: error("couldn't read user_id"),
                name = data["name"] as? String ?: error("couldn't read name"),
                createdAt = data["created_at"] as? Long ?: error("couldn't read created_at"),
            )
        }
    }
}
