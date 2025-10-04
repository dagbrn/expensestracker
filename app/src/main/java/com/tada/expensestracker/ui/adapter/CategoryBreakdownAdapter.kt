package com.tada.expensestracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tada.expensestracker.R
import com.tada.expensestracker.data.model.CategoryData
import java.text.NumberFormat
import java.util.Locale

class CategoryBreakdownAdapter : RecyclerView.Adapter<CategoryBreakdownAdapter.CategoryViewHolder>() {

    private var categoryList = listOf<CategoryData>()

    fun updateData(newCategoryList: List<CategoryData>) {
        val diffCallback = CategoryDiffCallback(categoryList, newCategoryList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        categoryList = newCategoryList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_breakdown, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int = categoryList.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCategoryAmount: TextView = itemView.findViewById(R.id.tvCategoryAmount)
        private val tvCategoryPercentage: TextView = itemView.findViewById(R.id.tvCategoryPercentage)
        private val tvTransactionCount: TextView = itemView.findViewById(R.id.tvTransactionCount)
        private val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)

        fun bind(categoryData: CategoryData) {
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            
            tvCategoryName.text = categoryData.categoryName
            tvCategoryAmount.text = "Rp ${formatter.format(categoryData.totalAmount.toLong())}"
            tvCategoryPercentage.text = "${String.format("%.1f", categoryData.percentage)}%"
            tvTransactionCount.text = "${categoryData.transactionCount} transaksi"
            
            // Set color indicator
            viewColorIndicator.setBackgroundColor(categoryData.color)
        }
    }

    private class CategoryDiffCallback(
        private val oldList: List<CategoryData>,
        private val newList: List<CategoryData>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].categoryName == newList[newItemPosition].categoryName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}