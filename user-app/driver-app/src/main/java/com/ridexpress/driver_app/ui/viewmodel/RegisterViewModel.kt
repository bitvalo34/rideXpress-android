package com.ridexpress.driver_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ridexpress.driver_app.services.FirebaseAuthService
import com.ridexpress.driver_app.services.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error(val msg: String) : RegisterState()
}

class RegisterViewModel(
    private val auth: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state

    fun setError(msg: String) {
        _state.value = RegisterState.Error(msg)
    }

    fun register(email: String, pass: String, username: String) = viewModelScope.launch {
        _state.value = RegisterState.Loading
        auth.registerWithEmail(email, pass)
            .onSuccess { user ->
                FirestoreService.createDriverDoc(user.uid, username, email)
                    .onSuccess { _state.value = RegisterState.Success(user) }
                    .onFailure { _state.value = RegisterState.Error(it.message ?: "Error DB") }
            }
            .onFailure { _state.value = RegisterState.Error(it.message ?: "Error Auth") }
    }

    fun registerWithGoogle(idToken: String, username: String) {
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            try {
                /* 0️⃣  Sign-in en Firebase Auth */
                val cred  = GoogleAuthProvider.getCredential(idToken, null)
                val user  = FirebaseAuth.getInstance()
                    .signInWithCredential(cred)
                    .await()
                    .user ?: throw Exception("Google no devolvió usuario")
                val uid   = user.uid

                /* 1️⃣  Documento en /drivers/{uid} */
                FirebaseFirestore.getInstance()
                    .collection("drivers").document(uid)
                    .set(
                        mapOf(
                            "uid"       to uid,
                            "email"     to (user.email ?: ""),
                            "username"  to username,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                    ).await()

                /* 2️⃣  Refrescar ID-token para acelerar el claim */
                user.getIdToken(true).await()

                _state.value = RegisterState.Success(user)      //  ← parámetro obligatorio
            } catch (e: Exception) {
                _state.value = RegisterState.Error(
                    e.message ?: "Error al registrar con Google"
                )
            }
        }
    }
}
