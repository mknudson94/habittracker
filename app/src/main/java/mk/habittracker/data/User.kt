package mk.habittracker.data

data class User(
    val id: Int,
    val email: String,
    val createdAt: Long,
    val userTimezone: java.time.ZoneId
)
