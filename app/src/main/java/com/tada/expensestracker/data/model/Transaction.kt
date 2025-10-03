package com.tada.expensestracker.data.model

import java.time.LocalDateTime
import java.util.Date

data class Transaction(
    var id: String? = null,
    var type: String = "",
    var amount: Double = 0.0,
    var note: String = "",
    var date: Long = System.currentTimeMillis()
)
