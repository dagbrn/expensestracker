package com.tada.expensestracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tada.expensestracker.data.model.ReportsData
import com.tada.expensestracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _reportsData = MutableLiveData<ReportsData?>()
    val reportsData: LiveData<ReportsData?> = _reportsData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Current selected period
    var selectedMonth: Int = 0
    var selectedYear: Int = 0

    init {
        // Set default to current month and year
        val calendar = Calendar.getInstance()
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedYear = calendar.get(Calendar.YEAR)
        fetchReportsData()
    }

    fun fetchReportsData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null)
            
            val result = repository.getReportsData(selectedMonth, selectedYear)
            
            if (result.isSuccess) {
                _reportsData.postValue(result.getOrNull())
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e("ReportsViewModel", "Error fetching reports data: $errorMsg")
                _error.postValue(errorMsg)
                _reportsData.postValue(null)
            }
            
            _isLoading.postValue(false)
        }
    }

    fun setSelectedPeriod(month: Int, year: Int) {
        if (selectedMonth != month || selectedYear != year) {
            selectedMonth = month
            selectedYear = year
            fetchReportsData()
        }
    }

    fun getFormattedPeriod(): String {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)
        
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        
        return "${monthNames[selectedMonth]} $selectedYear"
    }
}