package com.tada.expensestracker

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tada.expensestracker.ui.fragment.HomeFragment
import com.tada.expensestracker.ui.fragment.ReportsFragment
import com.tada.expensestracker.ui.fragment.SettingsFragment
import com.tada.expensestracker.ui.fragment.TransactionFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)
        
        // Auto sign-in user untuk setiap device
        autoSignInUser()

        // Default tampil HomeFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_transactions -> {
                    replaceFragment(TransactionFragment())
                    true
                }
                R.id.nav_reports -> {
                    replaceFragment(ReportsFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
    
    private fun autoSignInUser() {
        val app = application as ExpensesApp
        if (!app.authManager.isUserSignedIn()) {
            lifecycleScope.launch {
                val result = app.authManager.signInAsDeviceUser(this@MainActivity)
                if (result.isSuccess) {
                    Log.d("MainActivity", "Device user signed in: ${app.authManager.getCurrentUserId()}")
                } else {
                    Log.e("MainActivity", "Failed to sign in device user: ${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            Log.d("MainActivity", "User already signed in: ${app.authManager.getCurrentUserId()}")
        }
    }
}
