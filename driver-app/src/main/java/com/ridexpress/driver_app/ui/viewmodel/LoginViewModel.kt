package com.ridexpress.driver_app.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.ridexpress.driver_app.services.FirebaseAuthService
import com.ridexpress.driver_app.services.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val msg: String) : AuthState()
}

class LoginViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private suspend fun acceptOnlyDrivers(user: FirebaseUser) {
        if (FirestoreService.isDriver(user.uid)) {
            _state.value = AuthState.Success(user)
        } else {
            authService.signOut()
            _state.value = AuthState.Error("Esta cuenta no está registrada como conductor")
        }
    }

    private fun runCatchingToState(block: suspend () -> FirebaseUser) = viewModelScope.launch {
        _state.value = AuthState.Loading
        kotlin.runCatching { block() }
            .onSuccess { acceptOnlyDrivers(it) }
            .onFailure { _state.value = AuthState.Error(it.message ?: "Error desconocido") }
    }

    /* ─────── Email / Password ─────── */
    fun loginEmail(email: String, pass: String) = runCatchingToState {
        authService.signInWithEmail(email, pass).getOrThrow()
    }

    fun registerEmail(email: String, pass: String) = runCatchingToState {
        val user = authService.registerWithEmail(email, pass).getOrThrow()
        // crea documento del conductor SOLO si no existe
        FirestoreService.createDriverDoc(user.uid, email, email)
        user
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        authService.sendPasswordReset(email)
            .onSuccess { _state.value = AuthState.Idle }
            .onFailure { _state.value = AuthState.Error(it.message ?: "Error de envío") }
    }

    fun setError(msg: String) {
        _state.value = AuthState.Error(msg)
    }

    /* ─────── Google ─────── */
    fun loginGoogle(idToken: String) = runCatchingToState {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // FirebaseAuth global (no hay “auth” local)
        val user = FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .await()
            .user ?: throw Exception("No se pudo obtener el usuario de Google")

        // 🔄 refresca el ID-token para recibir el claim `role=driver`
        user.getIdToken(true).await()
        user                               //  ← este “user” lo recibe runCatching…
    }

    /* ---------- Teléfono ---------- */
    var storedVerificationId: String? = null

    fun startPhoneAuth(number: String, activity: Activity) {
        authService.startPhoneAuth(number, activity,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    loginWithCredential(credential)          // autoverificado
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _state.value = AuthState.Error(e.message ?: "Error OTP")
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = id
                }
            }
        )
    }

    fun verifyCode(code: String) {
        val id = storedVerificationId ?: return
        loginWithCredential(PhoneAuthProvider.getCredential(id, code))
    }

    private fun loginWithCredential(cred: AuthCredential) = runCatchingToState {
        FirebaseAuth.getInstance().signInWithCredential(cred).await().user!!
    }

}

