package com.arkindustries.gogreen.ui.views

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.ForgetPasswordRequest
import com.arkindustries.gogreen.api.request.ResetPasswordRequest
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.databinding.ActivitySignInBinding
import com.arkindustries.gogreen.databinding.ForgetPasswordBinding
import com.arkindustries.gogreen.databinding.ResetPasswordBinding
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.UserViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.UserSessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class SignIn : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var signInBinding: ActivitySignInBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var database: AppDatabase
    private lateinit var forgetPasswordDialog: Dialog
    private lateinit var forgetPasswordBinding: ForgetPasswordBinding
    private lateinit var resetPasswordBinding: ResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        signInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(signInBinding.root)

        database = AppDatabase.getInstance(this)

        val userService = RetrofitClient.createUserService(this)
        val userRepository = UserRepository(userService, database.userDao())
        userViewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        forgetPasswordDialog = Dialog(this)
        forgetPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        forgetPasswordDialog.setCanceledOnTouchOutside(false)
        forgetPasswordBinding = ForgetPasswordBinding.inflate(forgetPasswordDialog.layoutInflater)
        resetPasswordBinding = ResetPasswordBinding.inflate(forgetPasswordDialog.layoutInflater)

        val data: Uri? = intent.data
        val path = data?.path

        if (data != null && !path.isNullOrEmpty()) {
            when {
                "/api/v1/users/resetpassword".toRegex().matches(path) -> {
                    showResetPasswordDialog()
                    resetPasswordBinding.resetTokenTi.editText?.setText(data.pathSegments[0].toString())
                }
            }
        }

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

        signInBinding.forgetPasswordTv.setOnClickListener {
            showForgetPasswordDialog()
        }

        signInBinding.continueBtn.setOnClickListener {
            val email = signInBinding.emailTi.editText?.text.toString()
            val password = signInBinding.passwordTi.editText?.text.toString()

            hideKeyboard(this, signInBinding.emailTi)
            hideKeyboard(this, signInBinding.passwordTi)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.login(email, password)
                loginObserver()
            }
        }

        signInBinding.backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        userViewModel.loadingState.observe(this) {state ->
            if (state) {
                signInBinding.continueBtn.isEnabled = false
                signInBinding.progressBar.visibility = View.VISIBLE
                if (forgetPasswordDialog.isShowing) {
                    forgetPasswordDialog.findViewById<RelativeLayout>(R.id.progressBar).visibility = View.VISIBLE
                }
            } else {
                signInBinding.continueBtn.isEnabled = true
                signInBinding.progressBar.visibility = View.GONE
                if (forgetPasswordDialog.isShowing) {
                    forgetPasswordDialog.findViewById<RelativeLayout>(R.id.progressBar).visibility = View.GONE
                }
            }
        }
        resetPasswordObserver ()
        forgetPasswordObserver()
    }

    private fun loginObserver() {
        userViewModel.loginResult.observe(this) { response ->
            if (response.status == "success") {
                if (response.data != null) {
                    UserSessionManager.saveJwtToken(this, response.data.Jwt)
                    navigateToMainActivity()
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
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun resetPasswordObserver () {
        userViewModel.resetPasswordResult.observe(this) {
            if (it.status === "success") {
               resetPasswordBinding.passwordTi.setErrorTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)))
                resetPasswordBinding.passwordTi.editText?.error = it.message

                if (it.data != null) {
                    AppContext.getInstance().userSessionManager.saveJwtToken(this, it.data.Jwt)
                    navigateToMainActivity()
                }
            } else {
                resetPasswordBinding.passwordTi.setErrorTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, androidx.appcompat.R.color.error_color_material_dark)))
                resetPasswordBinding.passwordTi.editText?.error = it.message
            }
        }
    }

    private fun forgetPasswordObserver () {
        userViewModel.forgetPasswordResult.observe(this) {
            if (it.status === "success") {
                forgetPasswordBinding.emailTi.setErrorTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)))
                forgetPasswordBinding.emailTi.editText?.error = it.message

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        showResetPasswordDialog()
                    },
                    2000
                )
            } else {
                forgetPasswordBinding.emailTi.setErrorTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, androidx.appcompat.R.color.error_color_material_dark)))
                forgetPasswordBinding.emailTi.editText?.error = it.message
            }
        }
    }

    private fun showForgetPasswordDialog () {

        forgetPasswordBinding = ForgetPasswordBinding.inflate(forgetPasswordDialog.layoutInflater)
        forgetPasswordDialog.setContentView(forgetPasswordBinding.root)
        forgetPasswordDialog.show()

        forgetPasswordDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        forgetPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        forgetPasswordDialog.window?.attributes?.windowAnimations ?: R.style.DialogAnimation
        forgetPasswordDialog.window?.setGravity(Gravity.BOTTOM)


        forgetPasswordBinding.cancelButton.setOnClickListener {
            forgetPasswordDialog.dismiss()
        }

        forgetPasswordBinding.emailTi.editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                forgetPasswordBinding.emailTi.error = null
            }
        }

        forgetPasswordBinding.continueBtn.setOnClickListener {
            val email = forgetPasswordBinding.emailTi.editText?.text
            if (email.isNullOrEmpty()) {
                forgetPasswordBinding.emailTi.setErrorTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, androidx.appcompat.R.color.error_color_material_dark)))
                forgetPasswordBinding.emailTi.error = "Please provide your email address to continue"
                return@setOnClickListener
            }

            userViewModel.forgetPassword(ForgetPasswordRequest(email.toString()))
        }
    }

    private fun showResetPasswordDialog () {
        resetPasswordBinding = ResetPasswordBinding.inflate(forgetPasswordDialog.layoutInflater)
        forgetPasswordDialog.setContentView(resetPasswordBinding.root)
        forgetPasswordDialog.show()

        forgetPasswordDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        forgetPasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        forgetPasswordDialog.window?.attributes?.windowAnimations ?: R.style.DialogAnimation
        forgetPasswordDialog.window?.setGravity(Gravity.BOTTOM)

        resetPasswordBinding.resetTokenTi.editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                resetPasswordBinding.resetTokenTi.error = null
                resetPasswordBinding.passwordTi.error = null
            }
        }

        resetPasswordBinding.passwordTi.editText?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                resetPasswordBinding.resetTokenTi.error = null
                resetPasswordBinding.passwordTi.error = null
            }
        }

        resetPasswordBinding.resetBtn.setOnClickListener {
            val token = resetPasswordBinding.resetTokenTi.editText?.text.toString()
            val newPassword = resetPasswordBinding.passwordTi.editText?.text.toString()

            resetPasswordBinding.resetTokenTi.setErrorTextColor(
                ColorStateList.valueOf(ContextCompat.getColor(this, androidx.appcompat.R.color.error_color_material_dark)))
            resetPasswordBinding.passwordTi.setErrorTextColor(
                ColorStateList.valueOf(ContextCompat.getColor(this, androidx.appcompat.R.color.error_color_material_dark)))

            if (token.isEmpty()) {
                resetPasswordBinding.resetTokenTi.error = "Please provide reset token to continue"
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                resetPasswordBinding.passwordTi.error = "Please provide new password to continue"
                return@setOnClickListener
            }

            userViewModel.resetPassword(token, ResetPasswordRequest(newPassword))
        }

        resetPasswordBinding.cancelButton.setOnClickListener {
            forgetPasswordDialog.dismiss()
        }
    }

}