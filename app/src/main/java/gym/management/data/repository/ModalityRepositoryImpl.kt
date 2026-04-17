package gym.management.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import gym.management.data.source.FirestoreCollections
import gym.management.domain.model.Modality
import gym.management.domain.repository.ModalityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ModalityRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ModalityRepository {

    private val collection = firestore.collection(FirestoreCollections.MODALITIES)

    override fun observeAll(): Flow<List<Modality>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = collection.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val modalities = (snapshot?.toObjects(Modality::class.java) ?: emptyList()).map { m ->
                if (m.schedules.isEmpty() && m.schedule.isNotBlank()) m.copy(schedules = listOf(m.schedule))
                else m
            }
            trySend(modalities)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun save(modality: Modality): Result<Modality> = runCatching {
        val doc = collection.document()
        val saved = modality.copy(id = doc.id)
        doc.set(saved).await()
        saved
    }

    override suspend fun update(modality: Modality): Result<Unit> = runCatching {
        collection.document(modality.id).set(modality).await()
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        collection.document(id).delete().await()
    }
}
