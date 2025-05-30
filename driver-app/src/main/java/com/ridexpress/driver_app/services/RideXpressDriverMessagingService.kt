// driver-app/src/main/java/com/ridexpress/driver_app/service/RideXpressDriverMessagingService.kt
package com.ridexpress.driver_app.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ridexpress.driver_app.R

private const val CHANNEL_ID = "viajes_disponibles"
private const val NOTIF_ID   = 1001

class RideXpressDriverMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: subir el token a /users/{uid}/fcmToken
    }

    @SuppressLint("MissingPermission")   // hemos verificado permiso manualmente
    override fun onMessageReceived(message: RemoteMessage) {
        // 1 — ¿tengo permiso?
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return                         // evita SecurityException:contentReference[oaicite:5]{index=5}

        // 2 — ¿están habilitadas las notificaciones del sistema?
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        createChannelIfNeeded()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taxi)        // vector de Material Symbols
            .setContentTitle(message.notification?.title ?: "Nuevo viaje")
            .setContentText(message.notification?.body ?: "Tienes un viaje cercano")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIF_ID, notification)
    }

    /* ------------------------------------------------------------------------------------- */

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Viajes disponibles",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }
    }
}

