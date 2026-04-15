package gym.management.domain.model

data class Payment(
    val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val modalityId: String = "",
    val modalityName: String = "",
    val amount: Double = 0.0,
    val year: Int = 0,
    val month: Int = 0,
    val paymentDay: Int = 0,
    val isPaid: Boolean = true,
    val paidAt: Long? = null
)
