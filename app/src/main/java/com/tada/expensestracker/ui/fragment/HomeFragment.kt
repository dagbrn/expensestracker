package com.tada.expensestracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tada.expensestracker.ExpensesApp
import com.tada.expensestracker.R
import com.tada.expensestracker.data.repository.TransactionRepository
import com.tada.expensestracker.ui.activity.AddTransactionActivity
import com.tada.expensestracker.ui.adapter.TransactionAdapter
import com.tada.expensestracker.ui.viewmodel.HomeViewModel
import com.tada.expensestracker.ui.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var tvDate: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var adapter: TransactionAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = TransactionRepository()
        val factory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        //View & Adapter
        tvDate = view.findViewById(R.id.tv_date)
        tvBalance = view.findViewById(R.id.tv_balance)
        tvIncome = view.findViewById(R.id.tv_income)
        tvExpense = view.findViewById(R.id.tv_expense)
        rvTransactions = view.findViewById(R.id.rv_recent_transactions)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)
        adapter = TransactionAdapter(emptyList())
        rvTransactions.adapter = adapter
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()

        //add button
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
                    observeViewModel()
                } else {
                    android.util.Log.e("HomeFragment", "Authentication failed: ${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            android.util.Log.d("HomeFragment", "User already authenticated: ${app.authManager.getCurrentUserId()}")
            observeViewModel()
        }
    }

    private fun setupFAB() {
        fabAddTransaction.setOnClickListener {
            // Navigate to Add Transaction Activity
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }

        viewModel.todayDate.observe(viewLifecycleOwner) { todayDate ->
            tvDate.text = todayDate
        }

        viewModel.income.observe(viewLifecycleOwner) { income ->
            tvIncome.text = "Rp ${String.format("%,.0f",income)}"
        }

        viewModel.expense.observe(viewLifecycleOwner) { expense ->
            tvExpense.text = "Rp ${String.format("%,.0f", expense)}"
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            tvBalance.text = "Rp ${String.format("%,.0f", balance)}"
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }

    override fun onResume() {
        super.onResume()
        //dateTime loader
        viewModel.loadTransaction(
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )
    }
}