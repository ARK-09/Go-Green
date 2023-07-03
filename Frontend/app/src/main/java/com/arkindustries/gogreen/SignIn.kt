package com.arkindustries.gogreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arkindustries.gogreen.api.ApiClient
import com.arkindustries.gogreen.api.ApiService
import com.arkindustries.gogreen.api.request.LoginRequest
import com.arkindustries.gogreen.database.models.User
import com.arkindustries.gogreen.databinding.ActivitySignInBinding
import com.arkindustries.gogreen.util.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SignIn : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var signInBinding: ActivitySignInBinding
    private lateinit var apiService: ApiService
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        signInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(signInBinding.root)

        apiService = ApiClient.apiService
        coroutineScope = CoroutineScope(Dispatchers.IO)
        sharedPreferencesManager = SharedPreferencesManager(this)

        val jwtToken = sharedPreferencesManager.getJwtToken()
        if (jwtToken != null) {
           checkIfUserIsLoggedIn(jwtToken);
        }

        signInBinding.continueBtn.setOnClickListener {
            val email = signInBinding.emailTi.editText?.text.toString()
            val password = signInBinding.passwordTi.editText?.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                login(email, password)
            }
        }
    }
    private fun login(email: String, password: String) {
        coroutineScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.status == "success") {
                    val token = response.data?.token
                    if (token != null) {
                        sharedPreferencesManager.saveJwtToken(token)
                        navigateToHome()
                    }
                } else {
                    signInBinding.emailTi.error = response.message
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignIn, "Login failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    private fun checkIfUserIsLoggedIn (token: String) {
        coroutineScope.launch {
            try {
                val response = apiService.getCurrentUser("Bearer $token")
                if (response.status == "success") {
                    val user = response.data
                    if (user != null) {
                        // Navigate to home screen
                        navigateToHome()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignIn, "Failed to get current user", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}