package com.arkindustries.gogreen.ui.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.request.SignupRequest
import com.arkindustries.gogreen.data.models.UserViewModel
import com.arkindustries.gogreen.data.repositories.UserRepository
import com.arkindustries.gogreen.databinding.ActivitySignInBinding
import com.arkindustries.gogreen.databinding.ActivitySignUpBinding
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.UserSessionManager
import com.google.android.material.snackbar.Snackbar

class SignUp : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding.root)

        val userRepository = UserRepository(this)
        viewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        signUpBinding.backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            finish()
        }

        signUpBinding.fullNameTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearError()
                }
            }

        signUpBinding.emailTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearError()
                }
            }

        signUpBinding.passwordTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearError()
                }
            }

        signUpBinding.confirmPasswordTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearError()
                }
            }

        signUpBinding.phoneNoTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearError()
                }
            }

        signUpBinding.continueBtn.setOnClickListener {
            hideKeyboard(this, signUpBinding.fullNameTi)
            hideKeyboard(this, signUpBinding.emailTi)
            hideKeyboard(this, signUpBinding.passwordTi)
            hideKeyboard(this, signUpBinding.continueBtn)

            if (isValidSignUpData()) {
                val fullName = signUpBinding.fullNameTi.editText?.text.toString()
                val email = signUpBinding.emailTi.editText?.text.toString()
                val password = signUpBinding.passwordTi.editText?.text.toString()
                val phoneNo = signUpBinding.phoneNoTi.editText?.text.toString()
                val userType = if (signUpBinding.userTypeRg.checkedRadioButtonId == R.id.freelancer_rb) "talent" else "client"

                viewModel.signup(SignupRequest(fullName, email, password, userType, phoneNo, null))
                signUpObserver()
            }

        }

        viewModel.loadingState.observe(this) {state ->
            if (state) {
                signUpBinding.continueBtn.isEnabled = false
                signUpBinding.progressBar3.visibility = View.VISIBLE
            } else {
                signUpBinding.continueBtn.isEnabled = true
                signUpBinding.progressBar3.visibility = View.GONE
            }
        }
    }

    private fun clearError () {
        signUpBinding.errorTv.visibility = View.GONE
        signUpBinding.fullNameTi.error = null
        signUpBinding.emailTi.error = null
        signUpBinding.passwordTi.error = null
        signUpBinding.confirmPasswordTi.error = null
        signUpBinding.phoneNoTi.error = null
    }

    private fun signUpObserver () {
        viewModel.signupApiResponse.observe(this) { response ->
            if (response.status == "success") {
                if (response.data != null) {
                    UserSessionManager.saveJwtToken(this, response.data.Jwt)
                    Log.i(SignUp::class.qualifiedName, response.data.Jwt)
                    navigateToHome()
                } else {
                    Snackbar.make(
                        signUpBinding.root,
                        "Failed to retrieve your session token. Please try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                signUpBinding.errorTv.visibility = View.VISIBLE
                signUpBinding.errorTv.text = response.message
                Log.e(SignUp::class.qualifiedName, response.stack.toString())
            }
        }
    }

    private fun isValidSignUpData (): Boolean {
        val fullName = signUpBinding.fullNameTi.editText?.text.toString()
        val email = signUpBinding.emailTi.editText?.text.toString()
        val password = signUpBinding.passwordTi.editText?.text.toString()
        val confirmPassword = signUpBinding.confirmPasswordTi.editText?.text.toString()
        val phoneNo = signUpBinding.phoneNoTi.editText?.text.toString()

        if (fullName.isEmpty()) {
            signUpBinding.fullNameTi.error = "Full name is a required field."
            return false
        }

        if (email.isEmpty()) {
            signUpBinding.fullNameTi.error = "Email is required required field."
            return false
        }

        if (password.isEmpty()) {
            signUpBinding.passwordTi.error = "Password is required field."
            return false
        }

        if (password != confirmPassword) {
            signUpBinding.confirmPasswordTi.error = "Password and confirm password should be same"
            return false
        }

        if (phoneNo.isEmpty()) {
            signUpBinding.phoneNoTi.error = "Phone no is required field."
            return false
        }

        return true
    }

    private fun navigateToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}