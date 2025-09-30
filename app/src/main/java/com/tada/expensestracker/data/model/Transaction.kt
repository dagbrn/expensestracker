package com.tada.expensestracker.data.model

data class Transaction(
    var id: String? = null,
    var type: String = "",
    var amount: Double = 0.0,
    var note: String = "",
    var date: String = ""
)
