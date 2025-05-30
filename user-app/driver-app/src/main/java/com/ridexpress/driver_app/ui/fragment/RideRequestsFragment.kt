package com.ridexpress.driver_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ridexpress.driver_app.ui.components.RideRequestItem
import com.ridexpress.driver_app.ui.viewmodel.DriverAvailabilityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RideRequestsFragment : Fragment() {

    private val drvVm: DriverAvailabilityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val trips by drvVm.availableTrips.collectAsState()

            LazyColumn {
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
