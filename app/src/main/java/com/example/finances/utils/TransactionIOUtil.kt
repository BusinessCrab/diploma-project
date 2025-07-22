package com.example.finances.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.finances.Transaction
import org.json.JSONObject
import java.io.FileOutputStream

object TransactionIOUtil {
    fun exportTransaction(context: Context, transaction: Transaction, uri: Uri?) {
        if (uri == null) {
            showToast(context, "URI не найден")
            return
        }

        try {
            val json = JSONObject().apply {
                put("id", transaction.id)
                put("title", transaction.title)
                put("description", transaction.description)
                put("amount", transaction.amount)
                put("date", transaction.date)
                put("userEmail", transaction.userEmail)
            }

            val content = json.toString()
            Log.d("EXPORT_JSON", content)

            context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { stream ->
                    stream.write(content.toByteArray())
                    stream.flush()
                    Log.d("EXPORT_JSON", "Файл успешно записан")
                }
            }

            showToast(context, "Экспорт завершён")
        } catch (e: Exception) {
            showToast(context, "Ошибка экспорта: ${e.message}")
        }
    }

    fun importTransaction(context: Context, uri: Uri?): Transaction? {
        if (uri == null) return null

        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val jsonText = input.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)

            Transaction(
                id = json.optInt("id", 0),
                title = json.getString("title"),
                description = json.getString("description"),
                amount = json.getInt("amount"),
                date = json.getLong("date"),
                userEmail = json.getString("userEmail")
            )
        } catch (e: Exception) {
            showToast(context, "Ошибка импорта: ${e.message}")
            null
        }
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
