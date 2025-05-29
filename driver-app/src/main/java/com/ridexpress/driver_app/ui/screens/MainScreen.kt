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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.maps.android.compose.*
import com.ridexpress.driver_app.R
import com.ridexpress.driver_app.ui.components.BottomCard
import com.ridexpress.driver_app.ui.components.RoundedButton
import com.ridexpress.driver_app.ui.navigation.NavRoute

@Composable
fun MainScreen(
    navController: NavHostController,
    available: Boolean,
    onToggleAvailable: (Boolean) -> Unit
) {
    val context = LocalContext.current

    /* --- Permiso de ubicación --- */
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission)
        }
    }

    /* --- Estado de mapa --- */
    val cameraState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            com.google.android.gms.maps.model.LatLng(14.6349, -90.5069),
            14f
        )
    }

    Box(Modifier.fillMaxSize()) {

        /* -------- Mapa -------- */
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings  = MapUiSettings(myLocationButtonEnabled = false)
        ) {
            Marker(
                state = MarkerState(com.google.android.gms.maps.model.LatLng(14.6349, -90.5069)),
                draggable = false
            )
        }

        /* -------- Tarjeta inferior -------- */
        BottomCard(
            available = available,
            onToggle = onToggleAvailable,
            onAvailableRides = { navController.navigate(NavRoute.AvailableRides.route) },
            onRequests = { navController.navigate(NavRoute.MyRequests.route) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
