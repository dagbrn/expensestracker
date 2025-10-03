package com.tada.expensestracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.TransactionWithId

class TransactionAdapter(private var transactions: List<TransactionWithId>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_transaction_name)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val ivCategoryIcon: ImageView = itemView.findViewById(R.id.type_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    fun getCategoryIcon(type: String): Int {
        return when (type.lowercase()) {
            "makanan" -> R.drawable.ic_food
            "transportasi" -> R.drawable.ic_transportation
            "hiburan" -> R.drawable.ic_fun
            "kesehatan" -> R.drawable.ic_health
            "belanja" -> R.drawable.ic_shop
            "tagihan" -> R.drawable.ic_bill
            "pendidikan" -> R.drawable.ic_education
            "gaji" -> R.drawable.ic_salary
            "bonus" -> R.drawable.ic_bonus
            "freelance" -> R.drawable.ic_freelance
            "investasi" -> R.drawable.ic_invest
            "hadiah" -> R.drawable.ic_gift
            else -> R.drawable.ic_other
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transactionWithId = transactions[position]
        
        holder.tvName.text = transactionWithId.note.ifEmpty { "Transaction" }
        holder.tvCategory.text = transactionWithId.type
        holder.ivCategoryIcon.setImageResource(getCategoryIcon(transactionWithId.type))

        // Format amount
        val formattedAmount = if (transactionWithId.amount >= 0) {
            "+ Rp ${String.format("%,.0f", transactionWithId.amount)}"
        } else {
            "- Rp ${String.format("%,.0f", Math.abs(transactionWithId.amount))}"
        }
        holder.tvAmount.text = formattedAmount
        
        // Set color based on transaction type
        val context = holder.itemView.context
        if (transactionWithId.amount >= 0) {
            holder.tvAmount.setTextColor(context.resources.getColor(android.R.color.holo_green_dark))
        } else {
            holder.tvAmount.setTextColor(context.resources.getColor(android.R.color.holo_red_dark))
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<TransactionWithId>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}