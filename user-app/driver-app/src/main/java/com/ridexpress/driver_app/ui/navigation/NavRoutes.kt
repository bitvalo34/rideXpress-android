package com.ridexpress.driver_app.ui.navigation

sealed class NavRoute(val route: String, val label: String) {
    object Home           : NavRoute("home", "Inicio")
    object AvailableRides : NavRoute("available", "Viajes Disponibles")
    object MyRequests     : NavRoute("requests", "Mis peticiones")
    object Profile        : NavRoute("profile", "Perfil")
}
