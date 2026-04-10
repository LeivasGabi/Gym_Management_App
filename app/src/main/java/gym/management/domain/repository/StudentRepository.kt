package gym.management.domain.repository

import gym.management.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeAll(): Flow<List<Student>>
    suspend fun getById(id: String): Result<Student>
    suspend fun save(student: Student): Result<Student>
    suspend fun update(student: Student): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
