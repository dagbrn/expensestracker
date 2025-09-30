package com.tada.expensestracker.data.repository

import com.tada.expensestracker.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("transactions")

    suspend fun addTransaction(transaction: Transaction) {
        val docRef = collection.document()
        transaction.id = docRef.id
        docRef.set(transaction).await()
    }

    fun getAllTransactions(onSuccess: (List<Transaction>) -> Unit, onError: (Exception) -> Unit) {
        collection.orderBy("date")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                onSuccess(transactions)
            }
    }

    suspend fun deleteTransaction(id: String) {
        collection.document(id).delete().await()
    }
}