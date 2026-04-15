package gym.management.domain.model

data class Graduation(
    val id: String = "",
    val studentId: String = "",
    val modalityId: String = "",
    val modalityName: String = "",
    val belt: String = "",
    val generalGrade: String = "",
    val observation: String = "",
    val date: Long = System.currentTimeMillis()
)
