package gym.management.data.repository

import gym.management.domain.model.User
import gym.management.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: error("Falha ao autenticar")
        User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty())
    }

    override suspend fun register(email: String, password: String): Result<User> = runCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: error("Falha ao criar conta")
        User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty())
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null
}
