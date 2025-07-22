package com.example.finances.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finances.R
import com.example.finances.database.UserDatabaseHelper

class AuthActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val savedEmail = prefs.getString("user_email", null)

        if (savedEmail != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_auth)

        dbHelper = UserDatabaseHelper(this)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Login button logic
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            if (dbHelper.loginUser(email, password)) {
                showToast("Успешный вход")
                navigateToMain(email)
            } else {
                showToast("Неверные данные. Зарегистрируйтесь.")
            }
        }

        // Register-button logic
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            if (dbHelper.registerUser(email, password)) {
                showToast("Регистрация успешна")
                navigateToMain(email)
            } else {
                showToast("Пользователь уже существует")
            }
        }
    }

    // Function of Main-screen navigation
    private fun navigateToMain(email: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("user_email", email).apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // because we don't want to return to the auth-screen again...
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

