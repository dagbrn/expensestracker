package com.tada.expensestracker.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.Transaction
import com.tada.expensestracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var rbIncome: RadioButton
    private lateinit var rbExpense: RadioButton
    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etNote: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private val firebaseRepository = TransactionRepository()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Categories for dropdown
    private val expenseCategories = arrayOf(
        "Makanan", "Transportasi", "Hiburan", "Kesehatan", "Belanja", 
        "Tagihan", "Pendidikan", "Kebutuhan", "Lainnya"
    )
    
    private val incomeCategories = arrayOf(
        "Gaji", "Bonus", "Freelance", "Investasi", "Hadiah", "Lainnya"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        initViews()
        setupToolbar()
        setupCategoryDropdown()
        setupDatePicker()
        setupTransactionTypeListener()
        setupSaveButton()

        // Set default date to today
        etDate.setText(dateFormat.format(Date()))
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rgTransactionType = findViewById(R.id.rg_transaction_type)
        rbIncome = findViewById(R.id.rb_income)
        rbExpense = findViewById(R.id.rb_expense)
        etAmount = findViewById(R.id.et_amount)
        etCategory = findViewById(R.id.et_category)
        etNote = findViewById(R.id.et_note)
        etDate = findViewById(R.id.et_date)
        btnSave = findViewById(R.id.btn_save)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupCategoryDropdown() {
        // Default to expense categories
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
        etCategory.setAdapter(adapter)
    }

    private fun setupTransactionTypeListener() {
        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_income -> {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, incomeCategories)
                    etCategory.setAdapter(adapter)
                    etCategory.setText("", false)
                }
                R.id.rb_expense -> {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
                    etCategory.setAdapter(adapter)
                    etCategory.setText("", false)
                }
            }
        }
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val note = etNote.text.toString().trim()
        val date = etDate.text.toString().trim()

        // Validation
        if (amountText.isEmpty()) {
            etAmount.error = "Amount is required"
            return
        }

        if (category.isEmpty()) {
            etCategory.error = "Category is required"
            return
        }

        if (note.isEmpty()) {
            etNote.error = "Note is required"
            return
        }

        try {
            var amount = amountText.toDouble()
            
            // Make amount negative for expenses
            if (rbExpense.isChecked) {
                amount = -Math.abs(amount)
            } else {
                amount = Math.abs(amount)
            }

            val transaction = Transaction(
                type = category,
                amount = amount,
                note = note,
                date = date
            )

            // Save to Firebase
            lifecycleScope.launch {
                btnSave.isEnabled = false
                btnSave.text = "Saving..."

                val result = firebaseRepository.addTransaction(transaction)
                
                if (result.isSuccess) {
                    Toast.makeText(this@AddTransactionActivity, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddTransactionActivity, "Failed to save transaction: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }

                btnSave.isEnabled = true
                btnSave.text = "Save Transaction"
            }

        } catch (e: NumberFormatException) {
            etAmount.error = "Invalid amount"
        }
    }
}