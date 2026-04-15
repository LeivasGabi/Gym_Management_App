package gym.management.domain.repository

import gym.management.domain.model.Graduation
import kotlinx.coroutines.flow.Flow

interface GraduationRepository {
    fun observeByStudent(studentId: String): Flow<List<Graduation>>
    fun observeByModality(modalityId: String): Flow<List<Graduation>>
    suspend fun save(graduation: Graduation): Result<Graduation>
    suspend fun update(graduation: Graduation): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
