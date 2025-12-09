package com.example.budgetplanner.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry
import com.example.budgetplanner.databinding.ItemExpenseBinding
import com.example.budgetplanner.util.IconUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onDeleteClick: (ExpenseEntry) -> Unit
) : ListAdapter<ExpenseEntry, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    private var categories: List<Category> = emptyList()

    fun setCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: ExpenseEntry) {
            val category = categories.find { it.id == expense.categoryId }
            
            binding.tvAmount.text = String.format("$%.2f", expense.amount)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(Date(expense.createdAt))

            if (category != null) {
                binding.tvCategoryName.text = category.name
                binding.ivCategoryIcon.setImageResource(IconUtils.getIconResId(category.iconName))
                binding.ivCategoryIcon.backgroundTintList = ColorStateList.valueOf(category.colorValue)
            } else {
                binding.tvCategoryName.text = "Unknown"
            }
            
            binding.root.setOnLongClickListener {
                onDeleteClick(expense)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseEntry>() {
        override fun areItemsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
            return oldItem == newItem
        }
    }
}
