package gym.management.domain.repository

import gym.management.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Boolean
}
