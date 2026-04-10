package gym.management.domain.model

data class Student(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val plan: String = "",
    val active: Boolean = true,
    val registrationDate: Long = System.currentTimeMillis()
)
