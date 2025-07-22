package com.example.finances.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import com.example.finances.R
import com.example.finances.Transaction
import com.example.finances.database.TransactionDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

object TransactionDialogUtil {

    fun showAddTransactionDialog(
        activity: Activity,
        userEmail: String,
        database: TransactionDatabaseHelper,
        reloadCallback: () -> Unit,
        importLauncher: ActivityResultLauncher<Intent>,
        prefill: Transaction? = null
    ) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_add_transaction, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val dateEditText = dialogView.findViewById<EditText>(R.id.dateEditText)
        val addButton = dialogView.findViewById<Button>(R.id.addTransactionButtonDialog)
        val importButton = dialogView.findViewById<Button>(R.id.importTransactionButton)

        DateInputMask(dateEditText).also {
            dateEditText.addTextChangedListener(it)
        }

        prefill?.let {
            titleEditText.setText(it.title)
            descriptionEditText.setText(it.description)
            amountEditText.setText(it.amount.toString())
            dateEditText.setText(formatDate(it.date))
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Добавить транзакцию")
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val amountText = amountEditText.text.toString()
            val dateText = dateEditText.text.toString()

            if (title.isNotEmpty() && amountText.isNotEmpty()) {
                val amount = amountText.toIntOrNull() ?: 0
                val timestamp = parseDateToTimestamp(dateText)

                val transaction = Transaction(
                    title = title,
                    description = description,
                    amount = amount,
                    date = timestamp,
                    userEmail = userEmail
                )

                database.addTransaction(transaction)
                reloadCallback()
                dialog.dismiss()
            }
        }

        importButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            importLauncher.launch(intent)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showEditTransactionDialog(
        activity: Activity,
        transaction: Transaction,
        database: TransactionDatabaseHelper,
        reloadCallback: () -> Unit,
        exportLauncher: ActivityResultLauncher<Intent>
    ) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val dateEditText = dialogView.findViewById<EditText>(R.id.dateEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveTransactionButtonDialog)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteTransactionButton)
        val exportButton = dialogView.findViewById<Button>(R.id.exportTransactionButton)

        titleEditText.setText(transaction.title)
        descriptionEditText.setText(transaction.description)
        amountEditText.setText(transaction.amount.toString())
        dateEditText.setText(formatDate(transaction.date))

        DateInputMask(dateEditText).also {
            dateEditText.addTextChangedListener(it)
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Редактировать транзакцию")
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newDescription = descriptionEditText.text.toString()
            val newAmountText = amountEditText.text.toString()
            val newDateText = dateEditText.text.toString()

            if (newTitle.isNotEmpty() && newAmountText.isNotEmpty()) {
                val newAmount = newAmountText.toIntOrNull() ?: 0
                val newTimestamp = parseDateToTimestamp(newDateText)

                val updatedTransaction = transaction.copy(
                    title = newTitle,
                    description = newDescription,
                    amount = newAmount,
                    date = newTimestamp
                )

                database.updateTransaction(updatedTransaction)
                reloadCallback()
                dialog.dismiss()
            }
        }

        exportButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "${transaction.title}.json")
            }
            exportLauncher.launch(intent)
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            database.deleteTransaction(transaction.id)
            reloadCallback()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun parseDateToTimestamp(dateText: String): Long {
        return try {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            formatter.parse(dateText)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
