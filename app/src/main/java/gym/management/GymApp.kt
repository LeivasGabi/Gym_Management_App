package gym.management

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings

class GymApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureFirestore()
    }

    private fun configureFirestore() {
        try {
            val cacheSettings = PersistentCacheSettings.newBuilder()
                .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()

            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(cacheSettings)
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (_: Exception) {
        }
    }
}
