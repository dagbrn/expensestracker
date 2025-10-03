package com.tada.expensestracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tada.expensestracker.data.model.Transaction
import com.tada.expensestracker.data.model.TransactionWithId
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Get user-specific collection path
    private fun getUserTransactionsCollection() = 
        firestore.collection("users").document(getCurrentUserId()).collection("transactions")
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous_user"
    }
    
    suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            val documentRef = getUserTransactionsCollection().add(transaction).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransactionsByMonth(month: Int, year: Int): Result<List<TransactionWithId>> {
        return try {
            // Create date range for the month
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val endDate = calendar.timeInMillis

            val querySnapshot = getUserTransactionsCollection()
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = querySnapshot.documents.mapNotNull { document ->
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let { TransactionWithId(document.id, it) }
            }
            
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllTransactions(): Result<List<TransactionWithId>> {
        return try {
            val querySnapshot = getUserTransactionsCollection()
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = querySnapshot.documents.mapNotNull { document ->
                val transaction = document.toObject(Transaction::class.java)
                transaction?.let { TransactionWithId(document.id, it) }
            }
            
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransaction(transactionId: String, transaction: Transaction): Result<Unit> {
        return try {
            getUserTransactionsCollection().document(transactionId).set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            getUserTransactionsCollection().document(transactionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get monthly summary (income, expense, balance)
    suspend fun getMonthlySummary(month: Int, year: Int): Result<MonthlySummary> {
        return try {
            val transactionsResult = getTransactionsByMonth(month, year)
            if (transactionsResult.isSuccess) {
                val transactions = transactionsResult.getOrNull() ?: emptyList()
                var totalIncome = 0.0
                var totalExpense = 0.0
                
                transactions.forEach { transactionWithId ->
                    if (transactionWithId.amount > 0) {
                        totalIncome += transactionWithId.amount
                    } else {
                        totalExpense += Math.abs(transactionWithId.amount)
                    }
                }
                
                val balance = totalIncome - totalExpense
                val summary = MonthlySummary(totalIncome, totalExpense, balance)
                Result.success(summary)
            } else {
                Result.failure(transactionsResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)