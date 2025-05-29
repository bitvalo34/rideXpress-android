package com.ridexpress.driver_app.ui.activity

import android.content.Intent
import android.os.Bundle
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

class MainActivity : AuthenticatedActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                                navController       = navController,
                                available           = vm.available.collectAsState().value,
                                onToggleAvailable   = vm::setAvailable
                            )
                        }
                    )
                }
            }
        }
    }
}

