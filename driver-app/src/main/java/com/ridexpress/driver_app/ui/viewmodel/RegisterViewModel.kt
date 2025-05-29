package com.ridexpress.driver_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ridexpress.driver_app.services.FirebaseAuthService
import com.ridexpress.driver_app.services.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    fun registerWithGoogle(idToken: String, username: String) = viewModelScope.launch {
        _state.value = RegisterState.Loading
        auth.signInWithGoogle(idToken)
            .onSuccess { user ->
                FirestoreService.createDriverDoc(user.uid, username, user.email ?: "")
                    .onSuccess { _state.value = RegisterState.Success(user) }
                    .onFailure { _state.value = RegisterState.Error(it.message ?: "Error DB") }
            }
            .onFailure { _state.value = RegisterState.Error(it.message ?: "Error Google") }
    }
}
