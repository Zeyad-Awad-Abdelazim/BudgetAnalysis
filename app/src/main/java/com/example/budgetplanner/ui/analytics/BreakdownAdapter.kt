package com.example.budgetplanner.ui.analytics

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetplanner.databinding.ItemCategoryBreakdownBinding
import com.example.budgetplanner.util.IconUtils

class BreakdownAdapter : ListAdapter<CategoryBreakdown, BreakdownAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBreakdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemCategoryBreakdownBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryBreakdown) {
            binding.tvCategoryName.text = item.category.name
            binding.tvAmount.text = String.format("$%.2f", item.amount)
            binding.tvPercentage.text = String.format("%.1f%%", item.percentage)
            
            binding.ivCategoryIcon.setImageResource(IconUtils.getIconResId(item.category.iconName))
            binding.ivCategoryIcon.backgroundTintList = ColorStateList.valueOf(item.category.colorValue)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CategoryBreakdown>() {
        override fun areItemsTheSame(oldItem: CategoryBreakdown, newItem: CategoryBreakdown): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryBreakdown, newItem: CategoryBreakdown): Boolean {
            return oldItem == newItem
        }
    }
}
