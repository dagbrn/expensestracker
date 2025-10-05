package com.tada.expensestracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tada.expensestracker.data.repository.TransactionRepository

class HomeViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(ModelClass: Class<T>): T {
        if (ModelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}