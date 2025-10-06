package com.tada.expensestracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tada.expensestracker.data.model.Transaction
import com.tada.expensestracker.data.model.TransactionWithId
import com.tada.expensestracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel : ViewModel() {
    private val repository = TransactionRepository()
    private val _transactions = MutableLiveData<List<Transaction>?>()
    val transactions: LiveData<List<Transaction>?> = _transactions

    // Menyimpan bulan dan tahun yang sedang dipilih
    var selectedMonth: Int = 0
    var selectedYear: Int = 0

    init {
        // Set default ke bulan dan tahun saat ini
        val calendar = Calendar.getInstance()
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedYear = calendar.get(Calendar.YEAR)
        fetchTransactionsForSelectedPeriod()
    }

    fun fetchTransactionsForSelectedPeriod() {
        viewModelScope.launch {
            _transactions.postValue(null) // Tampilkan loading
            val result = repository.getTransactionsByMonth(selectedMonth, selectedYear)
            if (result.isSuccess) {
                val transactionsWithId = result.getOrNull() ?: emptyList()
                // Convert TransactionWithId to Transaction with id
                val transactions = transactionsWithId.map { transactionWithId ->
                    Transaction(
                        id = transactionWithId.id,
                        type = transactionWithId.type,
                        amount = transactionWithId.amount,
                        note = transactionWithId.note,
                        date = transactionWithId.date
                    )
                }
                _transactions.postValue(transactions)
            } else {
                Log.e("TransactionViewModel", "Error fetching transactions", result.exceptionOrNull())
                _transactions.postValue(emptyList())
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transaction.id?.let { id ->
                val result = repository.updateTransaction(id, transaction)
                if (result.isSuccess) {
                    fetchTransactionsForSelectedPeriod() // Refresh data
                } else {
                    Log.e("TransactionViewModel", "Error updating transaction", result.exceptionOrNull())
                }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(transactionId)
            if (result.isSuccess) {
                fetchTransactionsForSelectedPeriod() // Refresh data
            } else {
                Log.e("TransactionViewModel", "Error deleting transaction", result.exceptionOrNull())
            }
        }
    }
}