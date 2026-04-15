package gym.management.domain.repository

import gym.management.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observeByMonth(year: Int, month: Int): Flow<List<Payment>>
    suspend fun save(payment: Payment): Result<Payment>
    suspend fun delete(paymentId: String): Result<Unit>
}
