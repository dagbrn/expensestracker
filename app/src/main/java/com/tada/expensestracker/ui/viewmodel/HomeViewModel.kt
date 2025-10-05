package com.tada.expensestracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tada.expensestracker.data.model.TransactionWithId
import com.tada.expensestracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transaction = MutableLiveData<List<TransactionWithId>>()
    val transactions: LiveData<List<TransactionWithId>> get() = _transaction

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> get() = _income

    private val _expense = MutableLiveData<Double>()
    val expense: LiveData<Double> get() = _expense

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> get() = _todayDate

    init {
        val today = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        _todayDate.value = today
    }

    fun loadTransaction(month: Int, year: Int) {
        viewModelScope.launch {
            val result = repository.getTransactionsByMonth(month, year)
            if (result.isSuccess) {
                val data = result.getOrNull().orEmpty()
                _transaction.value = data
                calculateSummary(data)
            }
        }
    }

    private fun calculateSummary(transactions: List<TransactionWithId>) {
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach {
            if (it.amount > 0) totalIncome += it.amount
            else totalExpense += kotlin.math.abs(it.amount)
        }

        _income.value = totalIncome
        _expense.value = totalExpense
        _balance.value = totalIncome - totalExpense
    }
}