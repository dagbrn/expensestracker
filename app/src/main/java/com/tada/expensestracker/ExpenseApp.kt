package com.tada.expensestracker

import android.app.Application
import com.tada.expensestracker.data.repository.TransactionRepository
import com.tada.expensestracker.utils.AuthManager

class ExpensesApp : Application() {
    val repository: TransactionRepository by lazy { TransactionRepository() }
    val authManager: AuthManager by lazy { AuthManager() }
}