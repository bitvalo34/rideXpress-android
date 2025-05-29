package com.ridexpress.driver_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.ridexpress.driver_app.R
import com.ridexpress.driver_app.viewmodel.AuthState
import com.ridexpress.driver_app.viewmodel.LoginViewModel
import androidx.compose.ui.platform.LocalContext
import com.ridexpress.driver_app.ui.activity.RegisterActivity

/* -------------------------  Activity  ------------------------- */

class LoginActivity : ComponentActivity() {

    private lateinit var vm: LoginViewModel
    private lateinit var oneTapClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this)[LoginViewModel::class.java]
        oneTapClient = Identity.getSignInClient(this)
        val serverClientId = getString(R.string.default_web_client_id)

        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val cred = oneTapClient.getSignInCredentialFromIntent(result.data)
                cred.googleIdToken?.let(vm::loginGoogle)
            } catch (_: ApiException) { /* Handle */ }
        }

        setContent {
            val vm: LoginViewModel = viewModel()
            val state by vm.state.collectAsState()
            val context = LocalContext.current

            LoginScreen(
                state = state,
                onLogin  = { e, p -> vm.loginEmail(e, p) },
                onGoogle = {
                    oneTapClient.beginSignIn(
                        com.google.android.gms.auth.api.identity.BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                com.google.android.gms.auth.api.identity.BeginSignInRequest
                                    .GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(serverClientId)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            ).build()
                    ).addOnSuccessListener { res ->
                        googleLauncher.launch(
                            IntentSenderRequest.Builder(res.pendingIntent).build()
                        )
                    }
                },
                onRegister = { e, p -> vm.registerEmail(e, p) },
                onReset    = { e    -> vm.resetPassword(e) }
            )

            if (state is AuthState.Success) {
                LaunchedEffect(Unit) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

/* -------------------------  Composable  ------------------------- */

@Composable
fun LoginScreen(
    state: AuthState,
    onLogin: (String, String) -> Unit,
    onGoogle: () -> Unit,
    onRegister: (String, String) -> Unit,
    onReset: (String) -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    val orange = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(R.drawable.logo),    // recurso local
                contentDescription = "RideXpress logo",
                modifier = Modifier.size(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            /* -- Email -- */
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Default.Email, "correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            /* -- Password -- */
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, "contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            /* -- Botón login -- */
            Button(
                onClick = { onLogin(email.trim(), pass.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                modifier = Modifier.fillMaxWidth()
            ) { Text("INICIAR SESIÓN") }

            TextButton(onClick = { onReset(email.trim()) }) {
                Text("¿Olvidaste tu contraseña?")
            }

            /* -- Google -- */
            Button(
                onClick = onGoogle,
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = rememberAsyncImagePainter("https://developers.google.com/identity/images/g-logo.png"),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Iniciar con Google")
            }

            Spacer(Modifier.height(8.dp))

            /* -- Teléfono (solo UI) -- */
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("(+502) 1234‑5678") },
                leadingIcon = { Icon(Icons.Default.Phone, "sms") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { /* abrir modal OTP */ },
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sms), /* crea vector asset sms */
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Enviar SMS")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                }
            ) {
                Text("¿Aún no tienes cuenta? Regístrate.")
            }

        }

        /* -- Loader & Snackbar -- */
        if (state is AuthState.Loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        if (state is AuthState.Error) {
            Snackbar(Modifier.align(Alignment.BottomCenter)) { Text(state.msg) }
        }
    }
}

