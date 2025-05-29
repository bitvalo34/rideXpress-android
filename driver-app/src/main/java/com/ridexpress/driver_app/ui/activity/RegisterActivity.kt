package com.ridexpress.driver_app.ui.activity

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.ridexpress.driver_app.R
import com.ridexpress.driver_app.viewmodel.RegisterState
import com.ridexpress.driver_app.viewmodel.RegisterViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.Box
import com.google.android.gms.auth.api.identity.SignInClient

class RegisterActivity : ComponentActivity() {

    private lateinit var vm         : RegisterViewModel
    private lateinit var oneTap     : SignInClient
    private var usernameCache       = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 1. Inicializa ViewModel y One‑Tap  */
        vm     = ViewModelProvider(this)[RegisterViewModel::class.java]
        oneTap = Identity.getSignInClient(this)
        val serverClientId = getString(R.string.default_web_client_id)

        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { res ->
            try {
                val cred = oneTap.getSignInCredentialFromIntent(res.data)
                cred.googleIdToken?.let { idToken ->
                    vm.registerWithGoogle(idToken, usernameCache)
                }
            } catch (_: ApiException) { }
        }

        setContent {
            val vm: RegisterViewModel = viewModel()
            var username by remember { mutableStateOf("") }
            var email    by remember { mutableStateOf("") }
            var pass     by remember { mutableStateOf("") }
            val state by vm.state.collectAsState()

            RegisterScreen(
                username = username,
                email = email,
                pass = pass,
                onUsernameChange = { username = it },
                onEmailChange =    { email = it },
                onPassChange =     { pass = it },
                onRegister = { vm.register(email.trim(), pass.trim(), username.trim()) },
                onGoogle = {
                    usernameCache = username.trim()
                    oneTap.beginSignIn(
                        com.google.android.gms.auth.api.identity.BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                com.google.android.gms.auth.api.identity.BeginSignInRequest
                                    .GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(getString(R.string.default_web_client_id))
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            ).build()
                    ).addOnSuccessListener { r ->
                        googleLauncher.launch(
                            IntentSenderRequest.Builder(r.pendingIntent).build()
                        )
                    }
                },
                state = state,
                onSubmit = { u, e, p -> vm.register(e, p, u) },
                onBackToLogin = { finish() }
            )

            if (state is RegisterState.Success) finish()
        }
    }

    private lateinit var vm: RegisterViewModel
    private var usernameCache = ""
}

@Composable
fun RegisterScreen(
    username: String,
    email: String,
    pass: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onRegister: () -> Unit,
    onGoogle: () -> Unit,
    state: RegisterState,
    onBackToLogin: () -> Unit
) {
    val orange = colorResource(R.color.driver_orange)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("Registro con Correo", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = onPassChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onRegister,
            colors = ButtonDefaults.buttonColors(containerColor = orange),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrarse con Correo") }

        Spacer(Modifier.height(8.dp))
        Text("o")
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onGoogle,
            colors = ButtonDefaults.buttonColors(containerColor = orange.copy(alpha = .9f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(painterResource(R.drawable.ic_google_logo), null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Registrarse con Google")
        }

        Spacer(Modifier.height(8.dp))
        Text("o")

        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            label = { Text("(+502) 1234‑5678") },
            leadingIcon = { Icon(Icons.Default.Sms, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { /* future OTP */ },
            colors = ButtonDefaults.buttonColors(containerColor = orange.copy(alpha = .9f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(painterResource(R.drawable.ic_sms), null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Enviar SMS")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBackToLogin) { Text("¿Ya tienes cuenta? Inicia Sesión") }
    }

    if (state is RegisterState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state is RegisterState.Error) {
        Snackbar(Modifier.padding(8.dp)) { Text(state.msg) }
    }
}
