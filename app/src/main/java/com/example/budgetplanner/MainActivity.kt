package com.example.budgetplanner

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry
import com.example.budgetplanner.databinding.ActivityMainBinding
import com.example.budgetplanner.databinding.DialogAddExpenseBinding
import com.example.budgetplanner.ui.adapter.ExpenseAdapter
import com.example.budgetplanner.ui.analytics.AnalyticsActivity
import com.example.budgetplanner.ui.categories.ManageCategoriesActivity
import com.example.budgetplanner.util.IconUtils
import com.example.budgetplanner.viewmodel.BudgetViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    
    private var allExpenses: List<ExpenseEntry> = emptyList()
    private var allCategories: List<Category> = emptyList()
    
    private var currentFilterIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.topAppBar)
        
        setupRecyclerView()
        setupFilterSpinner()
        setupPieChart()
        setupObservers()
        setupListeners()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_manage_categories -> {
                startActivity(Intent(this, ManageCategoriesActivity::class.java))
                true
            }
            R.id.action_analytics -> {
                startActivity(Intent(this, AnalyticsActivity::class.java))
                true
            }
            R.id.action_add_expense_date -> {
                showAddExpenseDialog(Calendar.getInstance())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter { expense ->
            deleteExpense(expense)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = expenseAdapter
    }

    private fun setupFilterSpinner() {
        val filters = listOf("This Week", "This Month", "Last Month", "Last 90 Days")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        binding.spinnerFilter.adapter = adapter
        binding.spinnerFilter.setSelection(currentFilterIndex)
        
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilterIndex = position
                updateUI()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupPieChart() {
        binding.pieChart.description.isEnabled = false
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.setHoleColor(Color.TRANSPARENT)
        binding.pieChart.holeRadius = 40f
        binding.pieChart.transparentCircleRadius = 45f
    }

    private fun setupObservers() {
        viewModel.allCategories.observe(this) { categories ->
            allCategories = categories
            expenseAdapter.setCategories(categories)
            updateUI()
        }

        viewModel.allExpenses.observe(this) { expenses ->
            allExpenses = expenses
            updateUI()
        }
    }

    private fun setupListeners() {
        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog(null)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_manage_categories -> {
                    startActivity(Intent(this, ManageCategoriesActivity::class.java))
                    true
                }
                R.id.action_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    true
                }
                R.id.action_add_expense_date -> {
                    showAddExpenseDialog(Calendar.getInstance())
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI() {
        if (allExpenses.isEmpty() || allCategories.isEmpty()) return

        val filteredExpenses = filterExpenses(allExpenses, currentFilterIndex)
        
        val total = filteredExpenses.sumOf { it.amount }
        binding.tvTotalAmount.text = String.format("$%.2f", total)

        expenseAdapter.submitList(filteredExpenses)

        updateChart(filteredExpenses)
    }

    private fun filterExpenses(expenses: List<ExpenseEntry>, filterIndex: Int): List<ExpenseEntry> {
        val calendar = Calendar.getInstance()
        resetTime(calendar)

        val startDate: Long
        val endDate: Long

        when (filterIndex) {
            0 -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                resetTime(calendar)
                startDate = calendar.timeInMillis
                
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endDate = calendar.timeInMillis
            }
            1 -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                resetTime(calendar)
                startDate = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endDate = calendar.timeInMillis
            }
            2 -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                resetTime(calendar)
                startDate = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endDate = calendar.timeInMillis
            }
            3 -> {
                calendar.add(Calendar.DAY_OF_YEAR, -90)
                resetTime(calendar)
                startDate = calendar.timeInMillis
                
                val endCalendar = Calendar.getInstance()
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)
                endDate = endCalendar.timeInMillis
            }
            else -> {
                startDate = 0
                endDate = System.currentTimeMillis()
            }
        }

        return expenses.filter { 
            val expenseDate = toStartOfDay(it.createdAt)
            expenseDate >= startDate && expenseDate <= endDate 
        }
    }
    
    private fun toStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        resetTime(calendar)
        return calendar.timeInMillis
    }
    
    private fun resetTime(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun updateChart(expenses: List<ExpenseEntry>) {
        val categoryTotals = expenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        binding.chipGroupCategories.removeAllViews()
        categoryTotals.entries.sortedByDescending { it.value }.forEach { (catId, amount) ->
            val category = allCategories.find { it.id == catId } ?: return@forEach
            val chip = Chip(this)
            chip.text = "${category.name}: $${String.format("%.0f", amount)}"
            chip.setChipIconResource(IconUtils.getIconResId(category.iconName))
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#E8EAF6"))
            binding.chipGroupCategories.addView(chip)
        }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        categoryTotals.forEach { (catId, amount) ->
            val category = allCategories.find { it.id == catId }
            if (category != null) {
                entries.add(PieEntry(amount.toFloat(), category.name))
                colors.add(category.colorValue)
            }
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "Categories")
            dataSet.colors = colors
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.WHITE
            
            val data = PieData(dataSet)
            binding.pieChart.data = data
            binding.pieChart.invalidate()
            binding.pieChart.visibility = View.VISIBLE
        } else {
            binding.pieChart.visibility = View.GONE
        }
    }

    private fun deleteExpense(expense: ExpenseEntry) {
         MaterialAlertDialogBuilder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteExpense(expense)
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddExpenseDialog(defaultDate: Calendar?) {
        if (allCategories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val selectedCalendar = defaultDate ?: Calendar.getInstance()
        resetTime(selectedCalendar)
        
        val categoryNames = allCategories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = adapter

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dialogBinding.tvDateLabel.visibility = View.VISIBLE
        dialogBinding.tvSelectedDate.setText(dateFormat.format(selectedCalendar.time))
        
        dialogBinding.tvSelectedDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(year, month, dayOfMonth)
                    resetTime(selectedCalendar)
                    dialogBinding.tvSelectedDate.setText(dateFormat.format(selectedCalendar.time))
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle("Add Expense")
            .setPositiveButton("Save") { _, _ ->
                val amountStr = dialogBinding.etAmount.text.toString()
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                     Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                     return@setPositiveButton
                }

                val selectedCategoryIndex = dialogBinding.spinnerCategory.selectedItemPosition
                if (selectedCategoryIndex == -1) return@setPositiveButton
                val selectedCategory = allCategories[selectedCategoryIndex]

                resetTime(selectedCalendar)
                
                val expense = ExpenseEntry(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    categoryId = selectedCategory.id,
                    createdAt = selectedCalendar.timeInMillis
                )

                viewModel.insertExpense(expense)
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
