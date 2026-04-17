package gym.management.presentation.payment

import gym.management.domain.model.Modality
import gym.management.domain.model.Payment
import gym.management.domain.model.Student

data class StudentPaymentItem(
    val student: Student,
    val modality: Modality,
    val payment: Payment?,
    val isOverdue: Boolean = false
) {
    val isPaid: Boolean get() = payment != null
}

data class ModalityPaymentGroup(
    val modality: Modality,
    val students: List<StudentPaymentItem>,
    val isExpanded: Boolean
)

sealed class PaymentScreenState {
    data class YearPicker(val years: List<Int>) : PaymentScreenState()
    data class MonthPicker(val year: Int, val months: List<Int>) : PaymentScreenState()
    data class Detail(
        val year: Int,
        val month: Int,
        val groups: List<ModalityPaymentGroup>,
        val totalPaid: Double,
        val totalExpected: Double
    ) : PaymentScreenState()
    data class DetailError(val year: Int, val month: Int, val message: String) : PaymentScreenState()
}
