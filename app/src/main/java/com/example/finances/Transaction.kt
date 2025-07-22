package com.example.finances

data class Transaction(
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val amount: Int,
    val userEmail: String
)
