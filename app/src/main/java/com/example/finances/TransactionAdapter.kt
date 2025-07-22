package com.example.finances

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.textTitle)
        val descriptionText: TextView = itemView.findViewById(R.id.textDescription)
        val amountText: TextView = itemView.findViewById(R.id.textAmount)
        val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.titleText.text = transaction.title
        holder.descriptionText.text = transaction.description
        holder.amountText.text = "${transaction.amount} ₽"
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.dateTextView.text = formatter.format(Date(transaction.date))

        if (transaction.amount < 0) {
            holder.amountText.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        } else {
            holder.amountText.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        }

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = transactions.size
}
