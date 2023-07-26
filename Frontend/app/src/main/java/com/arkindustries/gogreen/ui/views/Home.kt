package com.arkindustries.gogreen.ui.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.data.models.UserViewModel
import com.arkindustries.gogreen.data.repositories.UserRepository
import com.arkindustries.gogreen.databinding.ActivityHomeBinding
import com.arkindustries.gogreen.databinding.ActivityMainBinding
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import com.arkindustries.gogreen.utils.UserSessionManager
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.snackbar.Snackbar

class Home : AppCompatActivity() {
    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        homeBinding.root.findViewById<View>(R.id.humburger_menu_ib).setOnClickListener {
            homeBinding.root.openDrawer(GravityCompat.START)
        }


        homeBinding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    UserSessionManager.clearJwtToken(this)
                    navigateToLogin()
                    true
                }
                else -> false
            }
        }

        val userRepository = UserRepository(this)
        viewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

    }

    private fun navigateToLogin() {
        val intent = Intent(this, SignIn::class.java)
        startActivity(intent)
        finish()
    }

}