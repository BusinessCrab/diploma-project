package com.example.finances.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.finances.R
import com.example.finances.database.TransactionDatabaseHelper
import com.example.finances.database.UserDatabaseHelper

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0) //Убираем анимацию
        setContentView(R.layout.activity_settings)

        setupBottomNavigation(R.id.nav_settings)

        val themeButton = findViewById<Button>(R.id.themeButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val deleteUserButton = findViewById<Button>(R.id.deleteUserButton)

        // Theme button initialization
        themeButton.setOnClickListener {
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val isDark = prefs.getBoolean("dark_theme", false)

            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            prefs.edit().putBoolean("dark_theme", !isDark).apply()
        }

        // Logout button initialization
        logoutButton.setOnClickListener {
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            prefs.edit().remove("user_email").apply()

            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        // Delete button initialization
        deleteUserButton.setOnClickListener {
            showDeleteUserDialog()
        }
    }

    // Delete dialog
    private fun showDeleteUserDialog() {
        AlertDialog.Builder(this)
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя и все его транзакции?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteUserAndTransactions()
            }
            .setNegativeButton("Отмена", null)
            .create()
            .show()
    }

    // User deletion function
    private fun deleteUserAndTransactions() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "") ?: ""

        val transactionDbHelper  = TransactionDatabaseHelper(this)
        val userDbHelper = UserDatabaseHelper(this)

        // Deleting all user's transactions
        transactionDbHelper.deleteAllTransactionsForUser(userEmail)

        // Deleting user from the table
        userDbHelper.deleteUser(userEmail)

        // Deleting auth data
        prefs.edit().clear().apply()

        // Starting Auth-activity
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}