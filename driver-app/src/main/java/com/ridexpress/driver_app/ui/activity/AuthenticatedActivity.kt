package com.ridexpress.driver_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ridexpress.driver_app.services.FirestoreService
import kotlinx.coroutines.launch

/**
 * Toda Activity que herede de esta clase
 * comprobará sesión antes de mostrarse.
 */
abstract class AuthenticatedActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            goToLogin()
            return
        }

        lifecycleScope.launch {
            if (!FirestoreService.isDriver(user.uid)) {
                FirebaseAuth.getInstance().signOut()
                goToLogin()
            }
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
