package com.example.finances.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.finances.Transaction

class TransactionDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "transactions.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "description TEXT," +
                    "date INTEGER," +
                    "amount INTEGER," +
                    "userEmail TEXT)"
        )
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        onCreate(db)
    }

    // Adding transaction function
    fun addTransaction(transaction: Transaction): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", transaction.title)
            put("description", transaction.description)
            put("date", transaction.date)
            put("amount", transaction.amount)
            put("userEmail", transaction.userEmail)
        }
        return db.insert("transactions", null, values) > 0
    }

    // Upgrade transaction function
    fun updateTransaction(transaction: Transaction): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", transaction.title)
            put("description", transaction.description)
            put("date", transaction.date)
            put("amount", transaction.amount)
        }
        return db.update("transactions", values, "id = ?", arrayOf(transaction.id.toString())) > 0
    }

    // Deleting transaction function
    fun deleteTransaction(id: Int): Boolean {
        val db = writableDatabase
        return db.delete("transactions", "id = ?", arrayOf(id.toString())) > 0
    }

    // Getting transaction function
    fun getTransactionsForUser(email: String): List<Transaction> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM transactions WHERE userEmail = ? ORDER BY date DESC",
            arrayOf(email)
        )

        val transactions = mutableListOf<Transaction>()
        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow("date")),
                    amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount")),
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail"))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    // Deleting all user's transactions function
    fun deleteAllTransactionsForUser(userEmail: String) {
        val db = writableDatabase
        db.delete("transactions", "userEmail = ?", arrayOf(userEmail))
    }
}