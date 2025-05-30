package com.ridexpress.driver_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.ridexpress.driver_app.data.TripRepository
import com.ridexpress.driver_app.model.DriverStatus
import com.ridexpress.driver_app.services.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverAvailabilityViewModel @Inject constructor(
    private val repo: TripRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _status = MutableStateFlow(DriverStatus.UNAVAILABLE)
    val status: StateFlow<DriverStatus> = _status
    private val location = locationService.lastLocationFlow

    /** Viajes ≤ 5 km cuando el conductor está disponible */
    val availableTrips = combine(repo.getAvailableTrips(), location) { trips, loc ->
        if (loc == null) emptyList()
        else trips.filter { t ->
            t.coordsOrigen != null &&
                    repo.distanceKm(
                        GeoPoint(loc.latitude, loc.longitude),
                        t.coordsOrigen!!
                    ) <= 5.0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleAvailability(on: Boolean) {
        _status.value = if (on) DriverStatus.AVAILABLE else DriverStatus.UNAVAILABLE
    }

    fun acceptTrip(id: String) = viewModelScope.launch {
        repo.acceptTrip(id)
    }
}

