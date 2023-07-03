package com.arkindustries.gogreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.arkindustries.gogreen.api.ApiClient
import com.arkindustries.gogreen.api.ApiService
import com.arkindustries.gogreen.api.request.SignUpRequest
import com.arkindustries.gogreen.databinding.ActivitySignUpBinding
import com.arkindustries.gogreen.util.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var apiService: ApiService
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(signUpBinding.root)

        apiService = ApiClient.apiService
        coroutineScope = CoroutineScope(Dispatchers.IO)
        sharedPreferencesManager = SharedPreferencesManager(this)

        signUpBinding.backBtn.setOnClickListener{
            finish()
        }

        signUpBinding.continueBtn.setOnClickListener {
            val fullName = signUpBinding.fullNameTi.editText?.text.toString()
            val email = signUpBinding.emailTi.editText?.text.toString()
            val password = signUpBinding.passwordTi.editText?.text.toString()
            val confirmPassword = signUpBinding.confirmPasswordTi.editText?.text.toString()

            if (password != confirmPassword) {
                signUpBinding.confirmPasswordTi.error = "Password and conform password should be same";
                return@setOnClickListener
            }
            val userType = if (signUpBinding.userTypeRg.checkedRadioButtonId == R.id.freelancer_rb) "freelancer" else "client"

            signUpUser(fullName, email, password, userType)
        }
    }

    private fun signUpUser(fullName: String, email: String, password: String, userType: String) {
        val signUpRequest = SignUpRequest(fullName, email, password, userType) // Replace with your actual sign-up request model

        coroutineScope.launch {
            try {
                val response = apiService.signUp(signUpRequest)
                if (response.status == "success") {
                    val token = response.data?.token
                    if (token != null) {
                        sharedPreferencesManager.saveJwtToken(token)
                        navigateToHome()
                    }
                } else {
                    signUpBinding.emailTi.error = response.message
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUp, "Login failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}