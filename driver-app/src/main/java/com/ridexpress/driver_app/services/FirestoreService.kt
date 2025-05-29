package com.ridexpress.driver_app.services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreService {

    suspend fun saveDriverProfile(uid: String, data: Map<String, Any>) =
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(data)
                .await()
        }

    suspend fun createDriverDoc(uid: String, username: String, email: String) =
        runCatching {
            FirebaseFirestore.getInstance()
                .collection("drivers")
                .document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "username" to username,
                        "email" to email,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        }

    suspend fun isDriver(uid: String): Boolean = try {
        val snap = FirebaseFirestore.getInstance()
            .collection("drivers")
            .document(uid)
            .get()
            .await()
        snap.exists()          // true si hay documento
    } catch (e: Exception) {
        false                  // ante error, trátalo como NO‑driver
    }

}

