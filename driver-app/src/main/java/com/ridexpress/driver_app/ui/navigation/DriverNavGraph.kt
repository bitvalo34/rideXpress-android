package com.ridexpress.driver_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ridexpress.driver_app.ui.screens.*

@Composable
fun DriverNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeContent: @Composable () -> Unit
) {
    NavHost(
        navController,
        startDestination = NavRoute.Home.route,
        modifier = modifier
    ) {
        composable(NavRoute.Home.route)           { homeContent() }
        composable(NavRoute.AvailableRides.route) { AvailableRidesScreen() }
        composable(NavRoute.MyRequests.route)     { RequestsScreen() }
        composable(NavRoute.Profile.route)        { ProfileScreen() }
    }
}

