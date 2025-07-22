package com.example.finances.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finances.R
import com.example.finances.Transaction
import com.example.finances.TransactionAdapter
import com.example.finances.database.TransactionDatabaseHelper
import com.example.finances.utils.TransactionDialogUtil
import com.example.finances.utils.TransactionIOUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : BaseActivity() {

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionDatabase: TransactionDatabaseHelper
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var totalExpensesTextView: TextView
    private lateinit var limitTextView: TextView
    private lateinit var changeLimitButton: Button
    private lateinit var addTransactionButton: FloatingActionButton
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private var transactionToExport: Transaction? = null

    private var currentUserEmail: String = ""
    private var currentLimit: Int = 0
    private var totalExpenses: Int = 0
    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0) // Deleting animation
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        setupBottomNavigation(R.id.nav_home)

        // Checking the Pass
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Getting the email of current user
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        currentUserEmail = prefs.getString("user_email", "") ?: ""

        // Getting the current limit-counter
        currentLimit = prefs.getInt("limit_$currentUserEmail", 5000)

        // UI-init
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView)
        limitTextView = findViewById(R.id.limitTextView)
        changeLimitButton = findViewById(R.id.changeLimitButton)
        addTransactionButton = findViewById(R.id.addTransactionButton)
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // BD-init
        transactionDatabase = TransactionDatabaseHelper(this)

        setupImportExportLaunchers()

        // Loading the transactions from list
        loadTransactions()

        // Adding-transaction button init
        addTransactionButton.setOnClickListener {
            TransactionDialogUtil.showAddTransactionDialog(
                activity = this,
                userEmail = currentUserEmail,
                database = transactionDatabase,
                reloadCallback = { loadTransactions() },
                importLauncher = importLauncher
            )
        }

        // Change limit button inti
        changeLimitButton.setOnClickListener {
            showChangeLimitDialog()
        }
    }

    // Import/export implementation
    private fun setupImportExportLaunchers() {
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                val transaction = TransactionIOUtil.importTransaction(this, uri)
                if (transaction != null) {
                    TransactionDialogUtil.showAddTransactionDialog(
                        activity = this,
                        userEmail = transaction.userEmail,
                        database = transactionDatabase,
                        reloadCallback = { loadTransactions() },
                        importLauncher = importLauncher,
                        prefill = transaction
                    )
                }
            }
        }

        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                transactionToExport?.let {
                    TransactionIOUtil.exportTransaction(this, it, uri)
                }
            }
        }
    }

    // Change limit dialog
    private fun showChangeLimitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_limit, null)

        val limitEditText = dialogView.findViewById<EditText>(R.id.limitEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveLimitButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Сменить лимит")
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newLimit = limitEditText.text.toString().toIntOrNull()
            if (newLimit != null) {
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit().putInt("limit_$currentUserEmail", newLimit).apply()
                currentLimit = newLimit
                loadTransactions()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Loading transaction from the list
    private fun loadTransactions() {
        transactions = transactionDatabase.getTransactionsForUser(currentUserEmail).sortedByDescending { it.date }
        totalExpenses = transactions.sumOf { it.amount }

        totalExpensesTextView.text = "Расходы: $totalExpenses ₽"
        limitTextView.text = "Лимит: $currentLimit ₽"

        // Красим лимит в красный, если превышен
        if (totalExpenses >= currentLimit) {
            limitTextView.setTextColor(getColor(android.R.color.holo_red_dark))
                /*if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                sendLimitExceededNotification()
            } */
        } else {
            limitTextView.setTextColor(getColor(android.R.color.black))
        }

        transactionAdapter = TransactionAdapter(transactions) { transaction ->
            transactionToExport = transaction
            TransactionDialogUtil.showEditTransactionDialog(
                activity = this,
                transaction = transaction,
                database = transactionDatabase,
                reloadCallback = { loadTransactions() },
                exportLauncher = exportLauncher
            )
        }
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionsRecyclerView.adapter = transactionAdapter
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Limit Channel"
            val descriptionText = "Уведомления о превышении лимита расходов"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("limit_channel", name, importance)
            channel.description = descriptionText;
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendLimitExceededNotification() {
        val builder = androidx.core.app.NotificationCompat.Builder(this, "limit_channel")
            .setContentTitle("Внимание!")
            .setContentText("Вы привысили лимит расходов!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(this)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1, builder.build())
        }
    }
}