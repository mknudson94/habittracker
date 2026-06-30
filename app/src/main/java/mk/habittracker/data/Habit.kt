package mk.habittracker.data

data class Habit(
    val id: Int,
    val userId: Int,
    val name: String,
    val createdAt: Long
)
