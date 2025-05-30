package com.ridexpress.driver_app.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ridexpress.driver_app.model.Trip
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor() {

    private val tripsRef = Firebase.firestore.collection("Viajes")
    private val uid get() = Firebase.auth.uid ?: ""

    /* ---------------------------------------------------------------------------
     * 1 · Viajes disponibles para cualquier conductor (status = requested)
     * ------------------------------------------------------------------------ */
    fun getAvailableTrips(): Flow<List<Trip>> = callbackFlow {
        val reg = tripsRef.whereEqualTo("status", "requested")
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    trySend(qs.documents.map { d ->
                        d.toObject(Trip::class.java)!!.copy(id = d.id)
                    })
                }
            }
        awaitClose { reg.remove() }
    }

    /* ---------------------------------------------------------------------------
     * 2 · Viajes activos de ESTE conductor (assigned o in_progress)
     * ------------------------------------------------------------------------ */
    fun getMyActiveTrips(): Flow<List<Trip>> = callbackFlow {
        val reg = tripsRef
            .whereEqualTo("driverId", uid)
            .whereIn("status", listOf("assigned", "in_progress"))
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    trySend(qs.documents.map { d ->
                        d.toObject(Trip::class.java)!!.copy(id = d.id)
                    })
                }
            }
        awaitClose { reg.remove() }
    }

    fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat/2).pow(2) + sin(dLon/2).pow(2) * cos(lat1) * cos(lat2)
        return 2 * r * asin(sqrt(h))
    }


    /* ---------------------------------------------------------------------------
     * 3 · Historial: viajes completados o cancelados por ESTE conductor
     * ------------------------------------------------------------------------ */
    fun getTripHistory(): Flow<List<Trip>> = callbackFlow {
        val reg = tripsRef
            .whereEqualTo("driverId", uid)
            .whereIn("status", listOf("completed", "cancelled"))
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    trySend(qs.documents.map { d ->
                        d.toObject(Trip::class.java)!!.copy(id = d.id)
                    })
                }
            }
        awaitClose { reg.remove() }
    }

    /* ---------------------------------------------------------------------------
     * 4 · Acción: aceptar un viaje ‘requested’ → ‘assigned’
     * ------------------------------------------------------------------------ */
    suspend fun acceptTrip(tripId: String) {
        val doc = tripsRef.document(tripId)
        Firebase.firestore.runTransaction { txn ->
            val snap = txn.get(doc)
            if (snap.getString("status") == "requested") {
                txn.update(doc, mapOf(
                    "status" to "assigned",
                    "driverId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            } else {
                throw IllegalStateException("Viaje ya no está disponible")
            }
        }.await()
    }

    /* ---------------------------------------------------------------------------
     * 5 · Acción: finalizar el viaje actual (assigned / in_progress → completed)
     * ------------------------------------------------------------------------ */
    suspend fun completeTrip(tripId: String) {
        val doc = tripsRef.document(tripId)
        Firebase.firestore.runTransaction { txn ->
            val snap = txn.get(doc)
            val currentStatus = snap.getString("status")
            val isMyTrip = snap.getString("driverId") == uid
            if (isMyTrip && currentStatus in listOf("assigned", "in_progress")) {
                txn.update(doc, mapOf(
                    "status" to "completed",
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            } else {
                throw IllegalStateException("No puedes completar este viaje")
            }
        }.await()
    }
}

