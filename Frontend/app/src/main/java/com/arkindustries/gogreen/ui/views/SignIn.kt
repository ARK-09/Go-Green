package com.arkindustries.gogreen.ui.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.data.models.UserViewModel
import com.arkindustries.gogreen.data.repositories.UserRepository
import com.arkindustries.gogreen.databinding.ActivitySignInBinding
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.UserSessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class SignIn : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var signInBinding: ActivitySignInBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        signInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(signInBinding.root)

        val userRepository = UserRepository(this)
        viewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        signInBinding.emailTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    signInBinding.errorTv.visibility = View.GONE
                    signInBinding.emailTi.error = null
                    signInBinding.passwordTi.error = null
                }
            }

        signInBinding.passwordTi.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    signInBinding.errorTv.visibility = View.GONE
                    signInBinding.emailTi.error = null
                    signInBinding.passwordTi.error = null
                }
            }

        signInBinding.continueBtn.setOnClickListener {
            val email = signInBinding.emailTi.editText?.text.toString()
            val password = signInBinding.passwordTi.editText?.text.toString()

            hideKeyboard(this, signInBinding.emailTi)
            hideKeyboard(this, signInBinding.passwordTi)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
                loginObserver()
            }
        }

        signInBinding.backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.loadingState.observe(this) {state ->
            if (state) {
                signInBinding.continueBtn.isEnabled = false
                signInBinding.progressBar.visibility = View.VISIBLE
            } else {
                signInBinding.continueBtn.isEnabled = true
                signInBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loginObserver() {
        viewModel.loginResult.observe(this) { response ->
            if (response.status == "success") {
                if (response.data != null) {
                    UserSessionManager.saveJwtToken(this, response.data.Jwt)
                    Log.i(SignIn::class.qualifiedName, response.data.Jwt)
                    navigateToHome()
                } else {
                    Snackbar.make(
                        signInBinding.root,
                        "Failed to retrieve your session token. Please try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                signInBinding.errorTv.visibility = View.VISIBLE

                signInBinding.emailTi.isErrorEnabled = false
                signInBinding.passwordTi.isErrorEnabled = false
                signInBinding.emailTi.error = " "
                signInBinding.passwordTi.error = " "

                signInBinding.errorTv.text = response.message
                Log.e(SignIn::class.qualifiedName, response.stack.toString())
            }
        }
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