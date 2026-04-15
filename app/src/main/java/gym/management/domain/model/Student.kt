package gym.management.domain.model

data class Student(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val birthDate: String = "",
    val emergencyContactName: String = "",
    val emergencyContact: String = "",
    val paymentDay: Int = 0,
    val modalityIds: List<String> = emptyList(),
    val active: Boolean = true,
    val registrationDate: Long = System.currentTimeMillis()
)
