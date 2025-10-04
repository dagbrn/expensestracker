package com.tada.expensestracker.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.CategoryData
import com.tada.expensestracker.data.model.ReportsData
import com.tada.expensestracker.ui.adapter.CategoryBreakdownAdapter
import com.tada.expensestracker.ui.viewmodel.ReportsViewModel
import java.text.NumberFormat
import java.util.*

class ReportsFragment : Fragment() {

    private val viewModel: ReportsViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryBreakdownAdapter

    // Views
    private lateinit var btnDateRange: MaterialButton
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var pieChart: PieChart
    private lateinit var rvCategoryBreakdown: RecyclerView
    private lateinit var layoutEmptyState: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupPieChart()
        setupDateNavigation()
        observeViewModel()
    }

    private fun initViews(view: View) {
        btnDateRange = view.findViewById(R.id.btnDateRange)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
//        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        pieChart = view.findViewById(R.id.pieChart)
        rvCategoryBreakdown = view.findViewById(R.id.rvCategoryBreakdown)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryBreakdownAdapter()
        rvCategoryBreakdown.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            // Chart appearance
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            
            // Hole in center
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            
            // Center text
            setDrawCenterText(true)
            setCenterTextSize(14f)  // ✅ FIXED: Use setCenterTextSize() method
            setCenterTextColor(Color.BLACK)
            
            // Legend
            legend.isEnabled = false
            
            // Entry label
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupDateNavigation() {
        btnDateRange.text = viewModel.getFormattedPeriod()
        
        btnPreviousMonth.setOnClickListener {
            navigateMonth(-1)
        }
        
        btnNextMonth.setOnClickListener {
            navigateMonth(1)
        }
    }

    private fun navigateMonth(direction: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(viewModel.selectedYear, viewModel.selectedMonth, 1)
        calendar.add(Calendar.MONTH, direction)
        
        viewModel.setSelectedPeriod(
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR)
        )
        
        btnDateRange.text = viewModel.getFormattedPeriod()
    }

    private fun observeViewModel() {
        viewModel.reportsData.observe(viewLifecycleOwner) { reportsData ->
            if (reportsData != null) {
                displayReportsData(reportsData)
            } else {
                showEmptyState()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showEmptyState()
            }
        }
    }

    private fun displayReportsData(reportsData: ReportsData) {
        layoutEmptyState.visibility = View.GONE
        rvCategoryBreakdown.visibility = View.VISIBLE
        pieChart.visibility = View.VISIBLE

        // Update summary cards
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
//        tvTotalIncome.text = "Rp ${formatter.format(reportsData.totalIncome.toLong())}"
        tvTotalExpense.text = "Rp ${formatter.format(reportsData.totalExpense.toLong())}"

        // Setup pie chart
        setupPieChartData(reportsData)
        
        // Update category breakdown list
        categoryAdapter.updateData(reportsData.expenseCategories)
    }

    private fun setupPieChartData(reportsData: ReportsData) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Add income data
//        if (reportsData.totalIncome > 0) {
//            entries.add(PieEntry(reportsData.totalIncome.toFloat(), "Income"))
//            colors.add(Color.parseColor("#4CAF50"))
//        }

        // Add expense categories
        reportsData.expenseCategories.forEach { category ->
            if (category.totalAmount > 0) {
                entries.add(PieEntry(category.totalAmount.toFloat(), category.categoryName))
                colors.add(category.color)
            }
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter()
            }

            val data = PieData(dataSet)
            pieChart.data = data
            
            // Set center text
            val total = reportsData.totalExpense
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            pieChart.centerText = "Total\nRp ${formatter.format(total.toLong())}"
            
            pieChart.invalidate()
        } else {
            pieChart.clear()
        }
    }

    private fun showEmptyState() {
        layoutEmptyState.visibility = View.VISIBLE
        rvCategoryBreakdown.visibility = View.GONE
        pieChart.visibility = View.GONE
        pieChart.clear()
        
//        tvTotalIncome.text = "Rp 0"
        tvTotalExpense.text = "Rp 0"
    }
}