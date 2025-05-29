package com.ridexpress.driver_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.ridexpress.driver_app.R
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*  1. Muestra la pantalla Compose  */
        setContent { DriverSplash() }

        /*  2. Lanza navegación tras 1.5 s  */
        val next = if (FirebaseAuth.getInstance().currentUser == null)
            LoginActivity::class.java else MainActivity::class.java

        window.decorView.postDelayed(
            { startActivity(Intent(this, next)); finish() },
            1500
        )
    }
}

/* -------------------------------------------------------------------------- */
/* ---------------------------  Composable Splash  -------------------------- */
/* -------------------------------------------------------------------------- */

@Composable
fun DriverSplash() {

    /* Animación: escala de 0.6 → 1.0 y opacidad implícita */
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = tween(durationMillis = 800), label = "logoScale"
    )
    LaunchedEffect(Unit) {                // dispara animación al entrar
        startAnim = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.driver_background)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "logo RideXpress",
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale)          // aplica la animación
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Drivers",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

