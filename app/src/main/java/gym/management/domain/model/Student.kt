package gym.management.domain.model

data class Student(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val birthDate: String = "",
    val emergencyContact: String = "",
    val modalityIds: List<String> = emptyList(),
    val active: Boolean = true,
    val registrationDate: Long = System.currentTimeMillis()
)
