package com.mk.habittracker.core.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "user_id" to userId,
        "name" to name,
        "created_at" to createdAt,
    )

    companion object {
        fun from(id: String, data: Map<String, Any>?): Habit {
            data!!
            return Habit(
                id = id,
                userId = data["user_id"] as? String ?: error("couldn't read user_id"),
                name = data["name"] as? String ?: error("couldn't read name"),
                createdAt = data["created_at"] as? Long ?: error("couldn't read created_at"),
            )
        }
    }
}
