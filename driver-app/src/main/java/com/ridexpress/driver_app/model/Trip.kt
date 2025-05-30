package com.ridexpress.driver_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.RawValue

@Parcelize
@IgnoreExtraProperties
data class Trip(
    val id: String = "",
    val status: String = "",
    val driverId: String? = null,
    val riderId: String = "",
    val origen: String = "",
    val destino: String = "",
    val fare: String = "",
    val coordsOrigen: @RawValue GeoPoint? = null,
    val coordsDestino: @RawValue GeoPoint? = null,
    val updatedAt: Timestamp? = null
) : Parcelable
