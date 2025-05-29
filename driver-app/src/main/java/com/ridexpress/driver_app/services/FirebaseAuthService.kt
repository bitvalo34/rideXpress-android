package com.ridexpress.driver_app.services

import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await

/**
 * Encapsula toda la lógica de autenticación.
 * Se llamará desde los ViewModels para mantener las Activities limpias.
 */
class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /* -------- Sesión actual -------- */

    fun currentUser() = auth.currentUser

    fun signOut() = auth.signOut()

    /* -------- Correo / contraseña -------- */

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching { auth.signInWithEmailAndPassword(email, password).await().user!! }

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching { auth.createUserWithEmailAndPassword(email, password).await().user!! }

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching { auth.sendPasswordResetEmail(email).await() }

    /* -------- Google Sign‑In -------- */

    fun getGoogleCredential(idToken: String): AuthCredential =
        GoogleAuthProvider.getCredential(idToken, null)

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> =
        runCatching { auth.signInWithCredential(getGoogleCredential(idToken)).await().user!! }

    /* -------- Teléfono (OTP) -------- */

    fun startPhoneAuth(
        phoneNumber: String,
        activity: android.app.Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(verificationId: String, code: String): Result<FirebaseUser> =
        runCatching {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential).await().user!!
        }

}
