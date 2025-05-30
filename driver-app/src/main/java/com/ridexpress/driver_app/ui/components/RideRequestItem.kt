package com.ridexpress.driver_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ridexpress.driver_app.model.Trip

@Composable
fun RideRequestItem(
    trip: Trip,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onAccept() }
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("Origen: ${trip.origen}", style = MaterialTheme.typography.bodyMedium)
            Text("Destino: ${trip.destino}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("Q${trip.fare}", style = MaterialTheme.typography.titleMedium)
        }
    }
}