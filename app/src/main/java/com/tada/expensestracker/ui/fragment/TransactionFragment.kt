package com.tada.expensestracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tada.expensestracker.databinding.FragmentTransactionBinding
import com.tada.expensestracker.ui.adapter.TransactionHistoryAdapter
import com.tada.expensestracker.ui.viewmodel.TransactionViewModel
import java.text.DateFormatSymbols
import java.util.Calendar

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterDropdowns()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Menggunakan adapter baru yang mendukung pengelompokan tanggal
        transactionAdapter = TransactionHistoryAdapter()
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFilterDropdowns() {
        // Setup Month Dropdown
        val months = DateFormatSymbols().months.filter { it.isNotEmpty() }.map { it.capitalize() }
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.monthSelectorAutocomplete.setAdapter(monthAdapter)
        binding.monthSelectorAutocomplete.setText(months[viewModel.selectedMonth], false)

        // Setup Year Dropdown
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2000..currentYear).map { it.toString() }.reversed()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
        binding.yearSelectorAutocomplete.setAdapter(yearAdapter)
        binding.yearSelectorAutocomplete.setText(viewModel.selectedYear.toString(), false)

        // Listener untuk bulan
        binding.monthSelectorAutocomplete.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedMonth = position
            viewModel.fetchTransactionsForSelectedPeriod()
        }

        // Listener untuk tahun
        binding.yearSelectorAutocomplete.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedYear = years[position].toInt()
            viewModel.fetchTransactionsForSelectedPeriod()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            when {
                transactions == null -> { // Loading
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                    binding.tvNoTransactions.visibility = View.GONE
                }
                transactions.isEmpty() -> { // Kosong
                    binding.progressBar.visibility = View.GONE
                    binding.rvTransactions.visibility = View.GONE
                    binding.tvNoTransactions.visibility = View.VISIBLE
                    setEmptyMessage()
                    transactionAdapter.updateData(emptyList())
                }
                else -> { // Sukses
                    binding.progressBar.visibility = View.GONE
                    binding.rvTransactions.visibility = View.VISIBLE
                    binding.tvNoTransactions.visibility = View.GONE
                    transactionAdapter.updateData(transactions)
                }
            }
        }
    }

    private fun setEmptyMessage() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        if (viewModel.selectedYear > currentYear ||
            (viewModel.selectedYear == currentYear && viewModel.selectedMonth > currentMonth)) {
            binding.tvNoTransactions.text = "Belum ada transaksi di bulan ini"
        } else {
            binding.tvNoTransactions.text = "Tidak ada transaksi di bulan ini"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}