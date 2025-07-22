package com.example.finances.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finances.R
import com.example.finances.Transaction
import com.example.finances.TransactionAdapter
import com.example.finances.database.TransactionDatabaseHelper
import com.example.finances.utils.TransactionDialogUtil
import com.example.finances.utils.TransactionIOUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : BaseActivity() {

    private lateinit var transactionDatabase: TransactionDatabaseHelper
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TransactionAdapter
    private lateinit var transactions: List<Transaction>
    private lateinit var selectDateButton: Button
    private lateinit var generateReportButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var savePdfLauncher: ActivityResultLauncher<Intent>

    private var transactionToExport: Transaction? = null
    private var pendingPdfDocument: PdfDocument? = null

    private var currentUserEmail: String = ""
    private var currentStartDate: Long? = null
    private var currentEndDate: Long? = null

    // List for transactions in report
    private var visibleTransaction: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0) //Deleting animation
        setContentView(R.layout.activity_history)

        setupBottomNavigation(R.id.nav_history)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        currentUserEmail = prefs.getString("user_email", "") ?: ""

        transactionDatabase = TransactionDatabaseHelper(this)

        selectDateButton = findViewById(R.id.selectDateButton)
        generateReportButton = findViewById(R.id.generateReportButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupImportExportLaunchers()
        setupPdfLauncher()

        loadTransactions()

        selectDateButton.setOnClickListener {
            showDateRangePicker()
        }

        generateReportButton.setOnClickListener {
            generatePdfReport()
        }
    }

    // Import/export-launchers setup(initialization of import/export logic)
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
                        reloadCallback = {
                            transactions = transactionDatabase.getTransactionsForUser(currentUserEmail)
                            historyAdapter.updateData(transactions)
                        },
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

    // Initialization of pdf logic
    private fun setupPdfLauncher() {
        savePdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    pendingPdfDocument?.let { doc ->
                        contentResolver.openOutputStream(uri)?.use { output ->
                            doc.writeTo(output)
                            showToast("PDF успешно сохранён")
                        }
                        doc.close()
                        pendingPdfDocument = null
                    }
                }
            }
        }
    }

    // Function of Transaction loading (date periods)
    private fun loadTransactions(start: Long? = null, end: Long? = null) {
        val all = transactionDatabase.getTransactionsForUser(currentUserEmail)
        val filtered = if (start != null && end != null) {
            all.filter { it.date in start..end }
        } else all

        transactions = filtered.sortedByDescending { it.date }
        visibleTransaction = transactions

        historyAdapter = TransactionAdapter(transactions) { transaction ->
            transactionToExport = transaction
            TransactionDialogUtil.showEditTransactionDialog(
                activity = this,
                transaction = transaction,
                database = transactionDatabase,
                reloadCallback = { loadTransactions(start, end) },
                exportLauncher = exportLauncher
            )
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    // Date-picker widget init
    private fun showDateRangePicker() {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите период")
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: return@addOnPositiveButtonClickListener
            val endDate = selection.second ?: return@addOnPositiveButtonClickListener

            currentStartDate = startDate
            currentEndDate = endDate

            filterTransactionsByDate(startDate, endDate)
        }
    }

    // Filtration transaction list by date
    private fun filterTransactionsByDate(startDate: Long, endDate: Long) {
        val filteredTransactions = transactions.filter {
            it.date in startDate..(endDate + 86399999L)
        }.sortedByDescending { it.date }

        visibleTransaction = filteredTransactions

        historyAdapter = TransactionAdapter(filteredTransactions) { transaction ->
            transactionToExport = transaction
            TransactionDialogUtil.showEditTransactionDialog(
                activity = this,
                transaction = transaction,
                database = transactionDatabase,
                reloadCallback = {
                    transactions = transactionDatabase.getTransactionsForUser(currentUserEmail)
                    filterTransactionsByDate(startDate, endDate)
                },
                exportLauncher = exportLauncher
            )
        }
        historyRecyclerView.adapter = historyAdapter
    }

    // Logic of pdf-file initialization
    private fun generatePdfReport() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = 50
        paint.textSize = 18f
        canvas.drawText("Отчёт по расходам", 40f, y.toFloat(), paint)

        y += 30
        paint.textSize = 14f

        val rangeText = if (currentStartDate != null && currentEndDate != null) {
            "Период: ${formatDate(currentStartDate!!)} — ${formatDate(currentEndDate!!)}"
        } else {
            "Период: всё время"
        }
        canvas.drawText(rangeText, 40f, y.toFloat(), paint)

        y += 30
        val total = visibleTransaction.sumOf { it.amount }
        canvas.drawText("Сумма расходов: $total ₽", 40f, y.toFloat(), paint)

        y += 30
        visibleTransaction.forEach {
            if (y > 800) return@forEach
            val line = "• [${formatDate(it.date)}] ${it.title} — ${it.amount} ₽"
            y += 25
            canvas.drawText(line, 40f, y.toFloat(), paint)
        }

        document.finishPage(page)
        pendingPdfDocument = document

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Отчёт.pdf")
        }
        savePdfLauncher.launch(intent)
    }

    //Date-formating function
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Toast-method (for simplification)
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
