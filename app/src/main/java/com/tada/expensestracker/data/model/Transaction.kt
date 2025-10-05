package com.tada.expensestracker.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Date

@Parcelize
data class Transaction(
    var id: String? = null,
    var type: String = "",
    var amount: Double = 0.0,
    var note: String = "",
    var date: Long = System.currentTimeMillis()
) : Parcelable

data class TransactionWithId(
    val id: String,
    val transaction: Transaction
) {
    val type: String get() = transaction.type
    val amount: Double get() = transaction.amount
    val note: String get() = transaction.note
    val date: Long get() = transaction.date
}

// Models untuk Reports
data class CategoryData(
    val categoryName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentage: Float,
    val color: Int = 0
)

data class ReportsData(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val expenseCategories: List<CategoryData>,
    val incomeData: CategoryData
)
