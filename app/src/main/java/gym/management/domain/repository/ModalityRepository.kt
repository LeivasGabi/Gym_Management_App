package gym.management.domain.repository

import gym.management.domain.model.Modality
import kotlinx.coroutines.flow.Flow

interface ModalityRepository {
    fun observeAll(): Flow<List<Modality>>
    suspend fun save(modality: Modality): Result<Modality>
    suspend fun delete(id: String): Result<Unit>
}
