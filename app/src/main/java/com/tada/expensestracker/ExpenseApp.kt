package com.tada.expensestracker

import android.app.Application
import com.tada.expensestracker.data.repository.TransactionRepository

class ExpensesApp : Application() {
    val repository: TransactionRepository by lazy { TransactionRepository() }
}