package com.ridexpress.driver_app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ridexpress.driver_app.model.DriverStatus
import com.ridexpress.driver_app.ui.components.BottomCard
import com.ridexpress.driver_app.ui.navigation.NavRoute
import com.ridexpress.driver_app.ui.viewmodel.DriverAvailabilityViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    vm: DriverAvailabilityViewModel = hiltViewModel()
) {
    /* ---------- Estado de conductor ---------- */
    val status by vm.status.collectAsState()
    val isAvailable = status == DriverStatus.AVAILABLE

    /* ---------- Permiso de ubicación ---------- */
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* podemos mostrar Snackbar si se niega */ }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* ---------- Estado del mapa ---------- */
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(14.6349, -90.5069), 14f
        )
    }

    Box(Modifier.fillMaxSize()) {

        val context = LocalContext.current
        val hasLocationPermission = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        /* -------- Mapa -------- */
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission.value),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false)
        ) {
            Marker(
                state = MarkerState(LatLng(14.6349, -90.5069)),
                draggable = false
            )
        }

        /* -------- Tarjeta inferior -------- */
        BottomCard(
            available = isAvailable,
            onToggle = { vm.toggleAvailability(it) },
            onAvailableRides = { navController.navigate(NavRoute.AvailableRides.route) },
            onRequests = { navController.navigate(NavRoute.MyRequests.route) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

