package com.tada.expensestracker.ui.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tada.expensestracker.R
import com.tada.expensestracker.ExpensesApp
import com.tada.expensestracker.ui.adapter.TransactionAdapter
import com.tada.expensestracker.data.model.Transaction
import com.tada.expensestracker.data.repository.FirebaseTransactionRepository
import com.tada.expensestracker.ui.activity.AddTransactionActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvDate: TextView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var llDateSection: LinearLayout
    private lateinit var fabAddTransaction: FloatingActionButton
    
    private lateinit var transactionAdapter: TransactionAdapter
    private val calendar = Calendar.getInstance()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val firebaseRepository = FirebaseTransactionRepository()
    
    // Current selected date for filtering and display
    private var selectedDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    private var selectedMonth: Int = calendar.get(Calendar.MONTH)
    private var selectedYear: Int = calendar.get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupDateSection()
        setupRecyclerView()
        setupFAB()
        
        // Wait for authentication before loading data
        ensureUserAuthenticated()
    }
    
    private fun ensureUserAuthenticated() {
        val app = requireActivity().application as ExpensesApp
        if (!app.authManager.isUserSignedIn()) {
            lifecycleScope.launch {
                val result = app.authManager.signInAsDeviceUser(requireContext())
                if (result.isSuccess) {
                    android.util.Log.d("HomeFragment", "User authenticated: ${app.authManager.getCurrentUserId()}")
                    loadData()
                } else {
                    android.util.Log.e("HomeFragment", "Authentication failed: ${result.exceptionOrNull()?.message}")
                    // Show sample data if authentication fails
                    loadSampleData()
                }
            }
        } else {
            android.util.Log.d("HomeFragment", "User already authenticated: ${app.authManager.getCurrentUserId()}")
            loadData()
        }
    }

    private fun loadSampleData() {
        val sampleTransactions = getSampleTransactions()
        val filteredTransactions = filterTransactionsByMonth(sampleTransactions, selectedMonth, selectedYear)
        updateSummary(filteredTransactions)
        transactionAdapter.updateTransactions(filteredTransactions)
        Toast.makeText(context, "Using sample data (authentication failed)", Toast.LENGTH_SHORT).show()
    }

    private fun initViews(view: View) {
        tvDate = view.findViewById(R.id.tv_date)
        tvMonthYear = view.findViewById(R.id.tv_month_year)
        tvBalance = view.findViewById(R.id.tv_balance)
        tvIncome = view.findViewById(R.id.tv_income)
        tvExpense = view.findViewById(R.id.tv_expense)
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions)
        llDateSection = view.findViewById(R.id.ll_date_section)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)
    }

    private fun setupDateSection() {
        updateDateDisplay()
        
        llDateSection.setOnClickListener {
            android.util.Log.d("HomeFragment", "Date section clicked")
            showDatePicker()
        }
    }

    private fun updateDateDisplay() {
        // Show current selected date, month and year
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
        
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        
        // Show actual selected date
        tvDate.text = dayFormat.format(calendar.time)
        tvMonthYear.text = monthYearFormat.format(calendar.time)
        
        android.util.Log.d("HomeFragment", "Date updated: ${tvDate.text} ${tvMonthYear.text}")
    }

    private fun showDatePicker() {
        android.util.Log.d("HomeFragment", "Showing date picker")
        // Create a date picker for full date selection
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                android.util.Log.d("HomeFragment", "Date selected: day=$dayOfMonth, month=$month, year=$year")
                selectedDay = dayOfMonth
                selectedMonth = month
                selectedYear = year
                updateDateDisplay()
                loadData() // Reload data for selected date
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        
        // Customize dialog title
        datePickerDialog.setTitle("Select Date")
        datePickerDialog.show()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList())
        rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvRecentTransactions.adapter = transactionAdapter
    }

    private fun setupFAB() {
        fabAddTransaction.setOnClickListener {
            // Navigate to Add Transaction Activity
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadData() {
        // Load data from Firebase based on selected month and year
        lifecycleScope.launch {
            try {
                val result = firebaseRepository.getTransactionsByMonth(selectedMonth, selectedYear)
                
                if (result.isSuccess) {
                    val transactions = result.getOrNull() ?: emptyList()
                    val sortedTransaction = transactions.sortedByDescending { it.date }
                    updateSummary(sortedTransaction)
                    transactionAdapter.updateTransactions(sortedTransaction)
                } else {
                    // Fallback to sample data if Firebase fails
                    val sampleTransactions = getSampleTransactions()
                    val filteredTransactions = filterTransactionsByMonth(sampleTransactions, selectedMonth, selectedYear)
                    updateSummary(filteredTransactions)
                    transactionAdapter.updateTransactions(filteredTransactions)
                    
                    Toast.makeText(context, "Using sample data. Firebase error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Fallback to sample data
                val sampleTransactions = getSampleTransactions()
                val filteredTransactions = filterTransactionsByMonth(sampleTransactions, selectedMonth, selectedYear)
                updateSummary(filteredTransactions)
                transactionAdapter.updateTransactions(filteredTransactions)
                
                Toast.makeText(context, "Using sample data. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterTransactionsByMonth(transactions: List<Transaction>, month: Int, year: Int): List<Transaction> {
        return transactions.filter { transaction ->
            // Parse date string (assuming format "yyyy-MM-dd")
            val formattedDate = Date(transaction.date)
            val transactionCal = Calendar.getInstance()
            transactionCal.time = formattedDate
            
            transactionCal.get(Calendar.MONTH) == month && 
            transactionCal.get(Calendar.YEAR) == year
        }
    }

//    private fun parseDate(dateString: Long): Date {
//        return try {
//            val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//            val date = Date(dateString)
//            format.parse(date) ?: Date()
//        } catch (e: Exception) {
//            Date()
//        }
//    }

    private fun updateSummary(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpense = 0.0
        
        transactions.forEach { transaction ->
            if (transaction.amount > 0) {
                totalIncome += transaction.amount
            } else {
                totalExpense += Math.abs(transaction.amount)
            }
        }
        
        val balance = totalIncome - totalExpense
        
        tvBalance.text = formatCurrency(balance)
        tvIncome.text = formatCurrency(totalIncome)
        tvExpense.text = formatCurrency(totalExpense)
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }

    private fun getSampleTransactions(): List<Transaction> {
        return listOf(
            // October 2025 transactions
            Transaction(id = "1", type = "Income", amount = 5000000.0, note = "Gaji", date = 1758789312345),
            Transaction(id = "2", type = "Makanan", amount = -25000.0, note = "Makan Siang", date = 1758789312345),
            Transaction(id = "3", type = "Transportasi", amount = -50000.0, note = "Bensin", date = 1758789312345),
            Transaction(id = "4", type = "Income", amount = 500000.0, note = "Bonus", date = 1758789312345),
            Transaction(id = "5", type = "Kebutuhan", amount = -350000.0, note = "Belanja Bulanan", date = 1758789312345),
            
            // September 2025 transactions (for testing month filter)
            Transaction(id = "6", type = "Income", amount = 4800000.0, note = "Gaji", date = 1758789312345),
            Transaction(id = "7", type = "Makanan", amount = -180000.0, note = "Groceries", date = 1758789312345),
            Transaction(id = "8", type = "Hiburan", amount = -150000.0, note = "Movie", date = 1758789312345),
            
            // November 2025 transactions (future month for testing)
            Transaction(id = "9", type = "Income", amount = 5200000.0, note = "Gaji", date = 1758789312345),
            Transaction(id = "10", type = "Investasi", amount = -1000000.0, note = "Saham", date = 1758789312345)
        )
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from add transaction activity
        loadData()
    }
}