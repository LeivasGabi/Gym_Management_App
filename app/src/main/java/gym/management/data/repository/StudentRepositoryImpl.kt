package gym.management.data.repository

import gym.management.data.source.FirestoreCollections
import gym.management.domain.model.Student
import gym.management.domain.repository.StudentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : StudentRepository {

    private val collection = firestore.collection(FirestoreCollections.STUDENTS)

    override fun observeAll(): Flow<List<Student>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val students = snapshot?.toObjects(Student::class.java) ?: emptyList()
            trySend(students)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getById(id: String): Result<Student> = runCatching {
        collection.document(id).get().await().toObject(Student::class.java)
            ?: error("Aluno não encontrado")
    }

    override suspend fun save(student: Student): Result<Student> = runCatching {
        val doc = collection.document()
        val newStudent = student.copy(id = doc.id)
        doc.set(newStudent).await()
        newStudent
    }

    override suspend fun update(student: Student): Result<Unit> = runCatching {
        collection.document(student.id).set(student).await()
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        collection.document(id).delete().await()
    }
}
