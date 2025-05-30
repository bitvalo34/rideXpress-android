package com.ridexpress.driver_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ridexpress.driver_app.R
import androidx.compose.ui.graphics.Color

@Composable
fun BottomCard(
    available: Boolean,
    onToggle: (Boolean) -> Unit,
    onAvailableRides: () -> Unit,
    onRequests: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.driver_orange)),
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Estado: ${if (available) "Disponible" else "Ocupado"}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = available,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00C853)
                )
            )
            Spacer(Modifier.height(16.dp))
            RoundedButton(
                text = "Ver viajes disponibles",
                onClick = onAvailableRides,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            RoundedButton(
                text = "Ver mis peticiones",
                onClick = onRequests,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp)
            )
        }
    }
}
