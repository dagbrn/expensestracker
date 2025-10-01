package com.tada.expensestracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.Transaction

class TransactionAdapter(private var transactions: List<Transaction>) : 
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_transaction_name)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        
        holder.tvName.text = transaction.note.ifEmpty { "Transaction" }
        holder.tvCategory.text = transaction.type
        
        // Format amount
        val formattedAmount = if (transaction.amount >= 0) {
            "+ Rp ${String.format("%,.0f", transaction.amount)}"
        } else {
            "- Rp ${String.format("%,.0f", Math.abs(transaction.amount))}"
        }
        holder.tvAmount.text = formattedAmount
        
        // Set color based on transaction type
        val context = holder.itemView.context
        if (transaction.amount >= 0) {
            holder.tvAmount.setTextColor(context.resources.getColor(android.R.color.holo_green_dark))
        } else {
            holder.tvAmount.setTextColor(context.resources.getColor(android.R.color.holo_red_dark))
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}