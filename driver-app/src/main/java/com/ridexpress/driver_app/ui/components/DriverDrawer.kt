package com.ridexpress.driver_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.ridexpress.driver_app.R
import com.ridexpress.driver_app.ui.navigation.NavRoute
import androidx.compose.ui.graphics.Color

@Composable
fun DriverDrawerContent(
    currentRoute: String,
    onNavigate: (NavRoute) -> Unit,
    onLogout: () -> Unit
) {
    val orange = colorResource(R.color.driver_orange)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )
        Text("Ir a…", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        Spacer(Modifier.height(32.dp))

        listOf(
            NavRoute.Home,
            NavRoute.AvailableRides,
            NavRoute.MyRequests,
            NavRoute.Profile
        ).forEach { route ->
            OutlinedButton(
                onClick = { onNavigate(route) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (currentRoute == route.route) orange.copy(alpha = 0.8f)
                    else Color.Transparent,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) { Text(route.label) }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape  = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cerrar Sesión", color = orange) }
    }
}
