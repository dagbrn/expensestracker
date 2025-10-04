package com.tada.expensestracker.data.model

import java.time.LocalDateTime
import java.util.Date

data class Transaction(
    var type: String = "",
    var amount: Double = 0.0,
    var note: String = "",
    var date: Long = System.currentTimeMillis()
)

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
