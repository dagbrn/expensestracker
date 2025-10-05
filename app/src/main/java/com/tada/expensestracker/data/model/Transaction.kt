package com.tada.expensestracker.data.model

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
