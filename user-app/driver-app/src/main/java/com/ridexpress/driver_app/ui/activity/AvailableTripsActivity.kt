package com.ridexpress.driver_app.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ridexpress.driver_app.ui.components.RideRequestItem
import com.ridexpress.driver_app.ui.viewmodel.DriverAvailabilityViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.ExperimentalMaterial3Api

@AndroidEntryPoint
class AvailableTripsActivity : AppCompatActivity() {

    private val drvVm: DriverAvailabilityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)                   // ⬅️ anotación
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val trips by drvVm.availableTrips.collectAsState()

            Scaffold(
                topBar = { CenterAlignedTopAppBar(title = { Text("Viajes cercanos") }) }
            ) { padding ->
                LazyColumn(contentPadding = padding) {
                    items(trips) { trip ->
                        RideRequestItem(
                            trip = trip,
                            onAccept = { drvVm.acceptTrip(trip.id) }
                        )
                    }
                }
            }
        }
    }
}
