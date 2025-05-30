package com.ridexpress.driver_app.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    /** Flujo con la última posición cada 5 s (o cuando cambia). */
    @SuppressLint("MissingPermission")            // ya comprobamos permiso manualmente
    val lastLocationFlow: Flow<Location?> = callbackFlow {
        // 1 · ¿tienes permiso?
        val fine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED &&
            coarse != PackageManager.PERMISSION_GRANTED
        ) {
            // Sin permiso: cerramos el flow sin emitir nada
            close()
            return@callbackFlow
        }

        // 2 · Configura las actualizaciones
        val callback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                trySend(res.lastLocation)
            }
        }

        fused.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000).build(),
            callback,
            Looper.getMainLooper()
        )

        awaitClose { fused.removeLocationUpdates(callback) }
    }
}


