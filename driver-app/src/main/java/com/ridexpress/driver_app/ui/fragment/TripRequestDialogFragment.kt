package com.ridexpress.driver_app.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ridexpress.driver_app.model.Trip
import com.ridexpress.driver_app.ui.viewmodel.DriverAvailabilityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TripRequestDialogFragment : DialogFragment() {

    private val vm: DriverAvailabilityViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val trip = requireArguments().getParcelable<Trip>("trip")!!
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo viaje")
            .setMessage(
                """
                Origen : ${trip.origen}
                Destino: ${trip.destino}
                Precio : Q${trip.fare}
                """.trimIndent()
            )
            .setPositiveButton("Aceptar") { _, _ -> vm.acceptTrip(trip.id) }
            .setNegativeButton("Ignorar", null)
            .create()
    }

    companion object {
        fun newInstance(t: Trip) = TripRequestDialogFragment().apply {
            arguments = bundleOf("trip" to t)
        }
    }
}
