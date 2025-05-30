package com.ridexpress.driver_app.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ridexpress.driver_app.R
import com.ridexpress.driver_app.ui.components.DriverDrawerContent
import com.ridexpress.driver_app.ui.navigation.DriverNavGraph
import com.ridexpress.driver_app.ui.navigation.NavRoute
import com.ridexpress.driver_app.ui.screens.MainScreen
import com.ridexpress.driver_app.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.*
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ridexpress.driver_app.ui.viewmodel.DriverAvailabilityViewModel
import com.ridexpress.driver_app.ui.fragment.TripRequestDialogFragment
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AuthenticatedActivity() {

    private val drvVm: DriverAvailabilityViewModel by viewModels()

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Para recibir viajes necesitas activar las notificaciones.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            drvVm.availableTrips.collectLatest { trips ->
                if (trips.isNotEmpty()) {
                    val first = trips.first()
                    if (supportFragmentManager.findFragmentByTag("trip_dialog") == null) {
                        TripRequestDialogFragment
                            .newInstance(first)
                            .show(supportFragmentManager, "trip_dialog")
                    }
                }
            }
        }

        /* ---------- Comprobación de permiso ---------- */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> { /* ya concedido */ }

                shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Permiso de notificaciones")
                        .setMessage(
                            "RideXpress necesita enviar notificaciones " +
                                    "para avisarte de viajes cercanos."
                        )
                        .setPositiveButton("Permitir") { _, _ ->
                            notifPermLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                            .setNegativeButton("Ahora no", null)
                            .show()
                }

                else -> notifPermLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        setContent {
            /* ---------- State ---------- */
            val vm: MainViewModel       = viewModel()
            val navController           = rememberNavController()
            val drawerState             = rememberDrawerState(DrawerValue.Closed)
            val scope                   = rememberCoroutineScope()
            val currentBackStackEntry   by navController.currentBackStackEntryAsState()
            val currentRoute            = currentBackStackEntry?.destination?.route ?: NavRoute.Home.route

            /* ---------- Drawer ---------- */
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DriverDrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finishAffinity()
                        }
                    )
                }
            ) {

                /* ---------- Scaffold ---------- */
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu Icon")
                                }
                            },
                            actions = {
                                /* Campana con badge rojo */
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "notificaciones",
                                        tint = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .background(Color.Red, CircleShape)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colorResource(R.color.driver_orange)
                            )
                        )
                    }
                ) { innerPadding ->

                    /* ---------- NavHost ---------- */
                    DriverNavGraph(
                        navController = navController,
                        modifier      = Modifier.padding(innerPadding),
                        homeContent   = {
                            MainScreen(
                                navController       = navController
                            )
                        }
                    )
                }
            }
        }
    }
}

