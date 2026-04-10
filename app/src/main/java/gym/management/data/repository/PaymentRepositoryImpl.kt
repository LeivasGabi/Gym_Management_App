package gym.management.data.repository

import gym.management.data.source.FirestoreCollections
import gym.management.domain.model.Payment
import gym.management.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaymentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PaymentRepository {

    private val collection = firestore.collection(FirestoreCollections.PAYMENTS)

    override fun observeAll(): Flow<List<Payment>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val payments = snapshot?.toObjects(Payment::class.java) ?: emptyList()
            trySend(payments)
        }
        awaitClose { listener.remove() }
    }

    override fun observeByStudent(studentId: String): Flow<List<Payment>> = callbackFlow {
        val listener = collection
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val payments = snapshot?.toObjects(Payment::class.java) ?: emptyList()
                trySend(payments)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun save(payment: Payment): Result<Payment> = runCatching {
        val doc = collection.document()
        val newPayment = payment.copy(id = doc.id)
        doc.set(newPayment).await()
        newPayment
    }

    override suspend fun update(payment: Payment): Result<Unit> = runCatching {
        collection.document(payment.id).set(payment).await()
    }
}
