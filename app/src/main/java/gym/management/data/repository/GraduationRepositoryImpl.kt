package gym.management.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import gym.management.data.source.FirestoreCollections
import gym.management.domain.model.Graduation
import gym.management.domain.repository.GraduationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GraduationRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GraduationRepository {

    private val collection = firestore.collection(FirestoreCollections.GRADUATIONS)

    override fun observeByStudent(studentId: String): Flow<List<Graduation>> = callbackFlow {
        val listener = collection
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Graduation::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // Firestore whereIn suporta até 30 valores; para academias com > 30 alunos
    // seria necessário dividir em múltiplas queries e mesclar os resultados.
    override fun observeByStudentIds(studentIds: List<String>): Flow<List<Graduation>> = callbackFlow {
        if (studentIds.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = collection
            .whereIn("studentId", studentIds.take(30))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Graduation::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override fun observeByModality(modalityId: String): Flow<List<Graduation>> = callbackFlow {
        val listener = collection
            .whereEqualTo("modalityId", modalityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Graduation::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun save(graduation: Graduation): Result<Graduation> = runCatching {
        val doc = collection.document()
        val saved = graduation.copy(id = doc.id)
        doc.set(saved).await()
        saved
    }

    override suspend fun update(graduation: Graduation): Result<Unit> = runCatching {
        collection.document(graduation.id).set(graduation).await()
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        collection.document(id).delete().await()
    }
}
