package com.arkindustries.gogreen.ui.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.UserService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.UserDao
import com.arkindustries.gogreen.databinding.ActivityHomeBinding
import com.arkindustries.gogreen.databinding.NavHeaderBinding
import com.arkindustries.gogreen.ui.repositories.UserRepository
import com.arkindustries.gogreen.ui.viewmodels.UserViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Home : AppCompatActivity() {
    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var toolbar: Toolbar
    private lateinit var database: AppDatabase
    private lateinit var userService: UserService
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var drawerHeaderBinding: NavHeaderBinding
    private var isUserClient: Boolean = false
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val debounceDelay: Long = 700

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        drawerHeaderBinding = NavHeaderBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        setupWithNavController(homeBinding.bottomNavigationView, navHostFragment.navController)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            homeBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        if (isUserClient) {
            homeBinding.createJobFab.visibility = View.VISIBLE
            homeBinding.bottomNavigationView.menu.clear()
            homeBinding.bottomNavigationView.inflateMenu(R.menu.client_bottom_menu)
            homeBinding.createJobFab.setOnClickListener {
                Navigation.findNavController(this, R.id.fragment_container).navigate(R.id.createJob)
            }
        }

        database = AppDatabase.getInstance(this)
        userService = RetrofitClient.createUserService(this)
        userDao = database.userDao()
        userRepository = UserRepository(userService, userDao)
        userViewModel =
            ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        homeBinding.root.findViewById<View>(R.id.humburger_menu_ib).setOnClickListener {
            homeBinding.root.openDrawer(GravityCompat.START)
        }

        drawerHeaderBinding.user = AppContext.getInstance().currentUser

        homeBinding.navView.addHeaderView(drawerHeaderBinding.root)
        homeBinding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Navigation.findNavController(this, R.id.fragment_container).navigate(R.id.jobsFragment)
                    handler.postDelayed(runnable, debounceDelay)
                    true
                }
                R.id.nav_account -> {
                    val intent = Intent(this, Account::class.java)
                    intent.putExtra("userId", AppContext.getInstance().currentUser.userId)
                    startActivity(intent)
                    handler.postDelayed(runnable, debounceDelay)
                    true
                }
                R.id.nav_edit_profile -> {
                    val intent = Intent(this, EditProfile::class.java)
                    intent.putExtra("userId", AppContext.getInstance().currentUser.userId)
                    startActivity(intent)
                    handler.postDelayed(runnable, debounceDelay)
                    true
                }
                R.id.nav_logout -> {
                    AppContext.getInstance().userSessionManager.clearJwtToken(this)
                    AppContext.clear()
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            AppDatabase.getInstance(this@Home).clearAllTables()
                        }
                    }
                    navigateToLogin()
                    homeBinding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                else -> false
            }
        }

//        homeBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.jobs -> {
//                    Navigation.findNavController(homeBinding.fragmentContainer).navigate(R.id.)
//                    replaceFragment(JobsFragment())
//                }
//                R.id.proposals -> replaceFragment(ProposalsFragment())
//                R.id.messages -> replaceFragment(Rooms())
//                R.id.notifications -> replaceFragment(Notification())
//                R.id.profile -> {
//                    val bundle = bundleOf()
//                    bundle.putBoolean("isProfileOwner", true)
//                    bundle.putString("userId", AppContext.getInstance().currentUser.userId)
//                    supportFragmentManager.setFragmentResult("profile", bundle)
//                    replaceFragment(Profile())
//                }
//            }
//
//            return@setOnItemSelectedListener true
//        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
        if (fragment is CreateJob) {
            transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
        }
        transaction.replace(homeBinding.fragmentContainer.id, fragment)
        transaction.addToBackStack(fragment::javaClass.name)
        transaction.commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, SignIn::class.java)
        startActivity(intent)
        finish()
    }
}