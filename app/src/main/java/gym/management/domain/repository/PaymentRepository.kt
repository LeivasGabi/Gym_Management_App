package gym.management.domain.repository

import gym.management.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observeAll(): Flow<List<Payment>>
    fun observeByStudent(studentId: String): Flow<List<Payment>>
    suspend fun save(payment: Payment): Result<Payment>
    suspend fun update(payment: Payment): Result<Unit>
}
