package com.tada.expensestracker.ui.activity

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.Transaction
import com.tada.expensestracker.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rgTransactionType: RadioGroup
    private lateinit var rbIncome: RadioButton
    private lateinit var rbExpense: RadioButton
    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etNote: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private val viewModel: TransactionViewModel by viewModels()
    private var existingTransaction: Transaction? = null
    private val calendar = Calendar.getInstance()

    private val expenseCategories = arrayOf("Makanan", "Transportasi", "Hiburan", "Kesehatan", "Belanja", "Tagihan", "Pendidikan", "Kebutuhan", "Lainnya")
    private val incomeCategories = arrayOf("Gaji", "Bonus", "Freelance", "Investasi", "Hadiah", "Lainnya")

    companion object {
        const val EXTRA_TRANSACTION = "extra_transaction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        initViews()
        setupToolbar()

        try {
            existingTransaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_TRANSACTION, Transaction::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_TRANSACTION)
            }

            if (existingTransaction == null) {
                Toast.makeText(this, "Gagal memuat data transaksi.", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            populateTransactionData()
            setupListeners()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
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
        btnUpdate = findViewById(R.id.btn_update)
        btnDelete = findViewById(R.id.btn_delete)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun populateTransactionData() {
        existingTransaction?.let { transaction ->
            try {
                if (transaction.amount >= 0) {
                    rbIncome.isChecked = true
                } else {
                    rbExpense.isChecked = true
                }

                val categories = if (rbIncome.isChecked) incomeCategories else expenseCategories
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
                etCategory.setAdapter(adapter)

                etAmount.setText(Math.abs(transaction.amount).toString())
                etCategory.setText(transaction.type ?: "", false)
                etNote.setText(transaction.note ?: "")
                etDate.setText(SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(transaction.date)))
                calendar.timeInMillis = transaction.date
            } catch (e: Exception) {
                Toast.makeText(this, "Error populating data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners(){
        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_income -> etCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, incomeCategories))
                R.id.rb_expense -> etCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories))
            }
            etCategory.setText("", false)
        }

        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etDate.setText(SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(calendar.time))
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnUpdate.setOnClickListener { updateTransaction() }
        btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun updateTransaction() {
        val amountText = etAmount.text.toString().trim()
        val categoryFromUI = etCategory.text.toString().trim()
        val note = etNote.text.toString().trim()

        if (amountText.isEmpty() || categoryFromUI.isEmpty()) {
            Toast.makeText(this, "Jumlah dan Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull() ?: 0.0
        val finalAmount = if (rbExpense.isChecked) -Math.abs(amount) else Math.abs(amount)

        val updatedTransaction = Transaction(
            id = existingTransaction?.id,
            type = categoryFromUI,
            amount = finalAmount,
            note = note,
            date = calendar.timeInMillis
        )

        viewModel.updateTransaction(updatedTransaction)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                existingTransaction?.id?.let { viewModel.deleteTransaction(it) }
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}