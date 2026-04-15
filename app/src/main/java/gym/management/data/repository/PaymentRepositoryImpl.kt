package gym.management.data.repository

import com.google.firebase.auth.FirebaseAuth
import gym.management.data.source.FirestoreCollections
import gym.management.domain.model.Payment
import gym.management.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaymentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : PaymentRepository {

    private val collection = firestore.collection(FirestoreCollections.PAYMENTS)

    override fun observeByMonth(year: Int, month: Int): Flow<List<Payment>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Payment::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun save(payment: Payment): Result<Payment> = runCatching {
        val doc = collection.document()
        val newPayment = payment.copy(id = doc.id)
        doc.set(newPayment).await()
        newPayment
    }

    override suspend fun delete(paymentId: String): Result<Unit> = runCatching {
        collection.document(paymentId).delete().await()
    }
}
