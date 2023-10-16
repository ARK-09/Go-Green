package com.arkindustries.gogreen.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.UserDao
import com.arkindustries.gogreen.databinding.ActivityMainBinding
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.UserViewModel
import com.arkindustries.gogreen.utils.UserSessionManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var userService: UserService
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary)

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        supportActionBar?.hide()

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        database = AppDatabase.getInstance(this)
        userService = RetrofitClient.createUserService(this)
        userDao = database.userDao()
        userRepository = UserRepository(userService, userDao)
        userViewModel = UserViewModel(userRepository)

        if (!UserSessionManager.getJwtToken(this).isNullOrEmpty()) {
            userViewModel.currentUser()
            currentUserObserver()
            currentUserErrorObserver()
            currentUserLoadingObserver()
        }

        mainBinding.signInBtn.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        mainBinding.createAccountBtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        mainBinding.retry.setOnClickListener {
            userViewModel.currentUser()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }

    private fun currentUserErrorObserver() {
        userViewModel.currentUserError.observe(this) {
            if (it.status == "fail" || it.status == "error") {
                Snackbar.make(mainBinding.root, it.message!!, Snackbar.LENGTH_LONG).show()
                mainBinding.retry.visibility = View.VISIBLE
            }
        }
    }

    private fun currentUserObserver() {
        userViewModel.currentUserResult.observe(this) { user ->
            AppContext.initialize(user)
            val jwtToken = AppContext.getInstance().userSessionManager.getJwtToken(this)
            if (jwtToken != null) {
                navigateToHome()
            }
        }
    }

    private fun currentUserLoadingObserver() {
        userViewModel.currentUserLoadingState.observe(this) {
            mainBinding.progressBar.visibility = if (it) {
                Glide.with(mainBinding.root).load(R.drawable.welcome_loading)
                    .into(mainBinding.loadingImageView)
                View.VISIBLE
            } else {
                View.GONE
            }

            mainBinding.signInBtn.isEnabled = !it
            mainBinding.createAccountBtn.isEnabled = !it
        }
    }
}