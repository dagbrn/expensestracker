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
    private val _transactions = MutableLiveData<List<TransactionWithId>?>()
    val transactions: LiveData<List<TransactionWithId>?> = _transactions

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
                _transactions.postValue(result.getOrNull())
            } else {
                Log.e("TransactionViewModel", "Error fetching transactions", result.exceptionOrNull())
                _transactions.postValue(emptyList())
            }
        }
    }
}