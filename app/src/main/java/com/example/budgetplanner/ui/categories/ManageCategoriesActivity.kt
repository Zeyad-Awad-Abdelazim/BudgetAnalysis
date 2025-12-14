package com.example.budgetplanner.ui.categories

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetplanner.R
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.databinding.ActivityManageCategoriesBinding
import com.example.budgetplanner.databinding.DialogAddCategoryBinding
import com.example.budgetplanner.databinding.ItemColorBinding
import com.example.budgetplanner.databinding.ItemIconBinding
import com.example.budgetplanner.util.IconUtils
import com.example.budgetplanner.viewmodel.BudgetViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeData()

        binding.fabAddCategory.setOnClickListener {
            showAddEditCategoryDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category -> showAddEditCategoryDialog(category) },
            onDeleteClick = { category -> deleteCategory(category) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun observeData() {
        viewModel.allCategories.observe(this) { categories ->
            adapter.submitList(categories)
            binding.tvEmptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun deleteCategory(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category,
                    onSuccess = { Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show() },
                    onError = { error -> Toast.makeText(this, error, Toast.LENGTH_LONG).show() }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddEditCategoryDialog(category: Category?) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialogBinding.tvDialogTitle.text = if (category == null) "Add Category" else "Edit Category"
        
        if (category != null) {
            dialogBinding.etCategoryName.setText(category.name)
        }

        var selectedIcon = category?.iconName ?: "receipt"
        val iconKeys = IconUtils.icons.keys.toList().filter { it != "default" }
        val iconAdapter = IconPickerAdapter(iconKeys, selectedIcon) { icon ->
            selectedIcon = icon
        }
        dialogBinding.rvIconPicker.layoutManager = GridLayoutManager(this, 5)
        dialogBinding.rvIconPicker.adapter = iconAdapter

        var selectedColor = category?.colorValue ?: Color.parseColor("#3F51B5")
        val colors = listOf(
            "#3F51B5",
            "#3949AB",
            "#7986CB",
            "#E91E63",
            "#FFC107",
            "#4CAF50",
            "#2196F3",
            "#9C27B0",
            "#FF9800",
            "#F44336"
        ).map { Color.parseColor(it) }
        
        val colorAdapter = ColorPickerAdapter(colors, selectedColor) { color ->
            selectedColor = color
        }
        dialogBinding.rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dialogBinding.rvColorPicker.adapter = colorAdapter

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etCategoryName.text.toString().trim()
                if (name.isEmpty()) {
                    dialogBinding.etCategoryName.error = "Name cannot be empty"
                    return@setOnClickListener
                }

                if (category == null) {
                    val newCategory = Category(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        iconName = selectedIcon,
                        colorValue = selectedColor
                    )
                    viewModel.insertCategory(newCategory)
                } else {
                    val updatedCategory = category.copy(
                        name = name,
                        iconName = selectedIcon,
                        colorValue = selectedColor
                    )
                    viewModel.updateCategory(updatedCategory)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    
    inner class IconPickerAdapter(
        private val icons: List<String>,
        private var selectedIcon: String,
        private val onIconSelected: (String) -> Unit
    ) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemIconBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iconName = icons[position]
            holder.binding.ivIcon.setImageResource(IconUtils.getIconResId(iconName))
            
            val isSelected = iconName == selectedIcon
            holder.binding.cardView.strokeWidth = if (isSelected) 4 else 0
            holder.binding.cardView.strokeColor = if (isSelected) 
                holder.itemView.context.getColor(android.R.color.holo_blue_dark) 
            else 
                0
            holder.binding.cardView.cardElevation = if (isSelected) 8f else 2f
            
            holder.itemView.setOnClickListener {
                val prev = selectedIcon
                selectedIcon = iconName
                onIconSelected(iconName)
                notifyItemChanged(icons.indexOf(prev))
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = icons.size
    }

    inner class ColorPickerAdapter(
        private val colors: List<Int>,
        private var selectedColor: Int,
        private val onColorSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val color = colors[position]
            
            val drawable = holder.itemView.context.getDrawable(R.drawable.circle_background)?.mutate()
            drawable?.setTint(color)
            holder.binding.viewColor.background = drawable
            
            val isSelected = color == selectedColor
            holder.binding.cardView.strokeWidth = if (isSelected) 4 else 2
            holder.binding.cardView.strokeColor = if (isSelected) 
                color 
            else 
                holder.itemView.context.getColor(android.R.color.darker_gray)
            holder.binding.cardView.cardElevation = if (isSelected) 8f else 2f
            
            holder.binding.viewColor.alpha = if (isSelected) 1.0f else 0.8f

            holder.itemView.setOnClickListener {
                val prev = selectedColor
                selectedColor = color
                onColorSelected(color)
                notifyItemChanged(colors.indexOf(prev))
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = colors.size
    }
}
