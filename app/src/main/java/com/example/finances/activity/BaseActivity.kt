package com.example.finances.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.finances.R
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    // Init of the button navigational panel
    fun setupBottomNavigation(selectedItemId: Int) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_theme", false)
        val bottomNavigationView = findViewById<BottomNavigationView?>(R.id.bottomNavigationView)

        // DarkMode setup
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Checking the existence of bottom navigational panel (caused lots of troubles in the past)
        if (bottomNavigationView != null) {
            bottomNavigationView.selectedItemId = selectedItemId

            // Nav-panel login realization
            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        if (this !is MainActivity) {
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(0, 0) //Deleting animation
                            finish()
                        }
                        true
                    }

                    R.id.nav_history -> {
                        if (this !is HistoryActivity) {
                            startActivity(Intent(this, HistoryActivity::class.java))
                            overridePendingTransition(0, 0) //Deleting animation
                            finish()
                        }
                        true
                    }

                    R.id.nav_settings -> {
                        if (this !is SettingsActivity) {
                            startActivity(Intent(this, SettingsActivity::class.java))
                            overridePendingTransition(0, 0) //Deleting animation
                            finish()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }
}
