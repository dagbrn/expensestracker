package com.tada.expensestracker.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Sealed class untuk menampung jenis item yang berbeda (Header Tanggal atau Item Transaksi)
sealed class TransactionListItem {
    data class DateHeader(val date: String) : TransactionListItem()
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
}

class TransactionHistoryAdapter(
    private var items: List<TransactionListItem> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionListItem.DateHeader -> TYPE_HEADER
            is TransactionListItem.TransactionItem -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            // PERBAIKAN 2: Menggunakan nama file layout yang benar
            val view = inflater.inflate(R.layout.item_date_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.list_item_transaction, parent, false)
            TransactionViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TransactionListItem.DateHeader -> (holder as DateViewHolder).bind(item)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(transactions: List<Transaction>) {
        val newItems = mutableListOf<TransactionListItem>()
        if (transactions.isNotEmpty()) {
            val sortedTransactions = transactions.sortedByDescending { it.date }
            var currentDate = ""
            val headerFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID"))
            val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (transaction in sortedTransactions) {
                val date = try { parseFormat.parse(transaction.date) } catch (e: Exception) { Date() }
                val transactionDate = headerFormat.format(date)

                if (transactionDate != currentDate) {
                    currentDate = transactionDate
                    newItems.add(TransactionListItem.DateHeader(currentDate))
                }
                newItems.add(TransactionListItem.TransactionItem(transaction))
            }
        }
        items = newItems
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTransactionName: TextView = itemView.findViewById(R.id.tv_transaction_name)
        private val tvTransactionType: TextView = itemView.findViewById(R.id.tv_transaction_type)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)

        fun bind(transaction: Transaction) {
            tvTransactionName.text = transaction.note
            tvTransactionType.text = transaction.type

            val formatter = NumberFormat.getInstance(Locale("in", "ID"))

            // PERBAIKAN 1: Mengembalikan logika ke pengecekan amount
            if (transaction.amount >= 0) {
                val formattedAmount = formatter.format(transaction.amount)
                tvTransactionAmount.text = "+ Rp $formattedAmount"
                tvTransactionAmount.setTextColor(Color.parseColor("#2e7d32"))
            } else {
                val formattedAmount = formatter.format(Math.abs(transaction.amount))
                tvTransactionAmount.text = "- Rp $formattedAmount"
                tvTransactionAmount.setTextColor(Color.parseColor("#c62828"))
            }
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDateHeader: TextView = itemView.findViewById(R.id.tv_date_header)
        fun bind(header: TransactionListItem.DateHeader) {
            tvDateHeader.text = header.date
        }
    }
}