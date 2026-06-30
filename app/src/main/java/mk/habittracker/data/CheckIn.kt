package mk.habittracker.data

data class CheckIn(
    val id: Int,
    val habitId: Int,
    val completedDate: java.time.LocalDate
)
