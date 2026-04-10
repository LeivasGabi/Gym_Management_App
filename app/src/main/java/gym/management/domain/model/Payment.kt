package gym.management.domain.model

data class Payment(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val amount: Double = 0.0,
    val dueDate: Long = System.currentTimeMillis(),
    val paidDate: Long? = null,
    val status: PaymentStatus = PaymentStatus.PENDING
)

enum class PaymentStatus {
    PENDING,
    PAID,
    OVERDUE
}
