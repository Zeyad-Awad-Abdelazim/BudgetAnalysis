package com.example.budgetplanner.ui.analytics

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry
import com.example.budgetplanner.databinding.ActivityAnalyticsBinding
import com.example.budgetplanner.viewmodel.BudgetViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var breakdownAdapter: BreakdownAdapter
    
    private var allExpenses: List<ExpenseEntry> = emptyList()
    private var allCategories: List<Category> = emptyList()
    
    private var startDate: Long = 0
    private var endDate: Long = 0
    private var isDateFilterEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupDatePickers()
        setupPieChart()
        observeData()
    }

    private fun setupRecyclerView() {
        breakdownAdapter = BreakdownAdapter()
        binding.rvBreakdown.layoutManager = LinearLayoutManager(this)
        binding.rvBreakdown.adapter = breakdownAdapter
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Default to current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        resetTime(calendar)
        startDate = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        endDate = calendar.timeInMillis

        binding.etStartDate.setText(dateFormat.format(startDate))
        binding.etEndDate.setText(dateFormat.format(endDate))

        binding.switchDateRange.setOnCheckedChangeListener { _, isChecked ->
            isDateFilterEnabled = isChecked
            binding.layoutDatePickers.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateUI()
        }

        binding.etStartDate.setOnClickListener {
            val c = Calendar.getInstance()
            c.timeInMillis = startDate
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                c.set(year, month, dayOfMonth)
                resetTime(c)
                startDate = c.timeInMillis
                binding.etStartDate.setText(dateFormat.format(startDate))
                updateUI()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etEndDate.setOnClickListener {
            val c = Calendar.getInstance()
            c.timeInMillis = endDate
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                c.set(year, month, dayOfMonth)
                c.set(Calendar.HOUR_OF_DAY, 23)
                c.set(Calendar.MINUTE, 59)
                c.set(Calendar.SECOND, 59)
                endDate = c.timeInMillis
                binding.etEndDate.setText(dateFormat.format(endDate))
                updateUI()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    
    private fun resetTime(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
    
    private fun resetTimeToStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        resetTime(calendar)
        return calendar.timeInMillis
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

    private fun observeData() {
        viewModel.allCategories.observe(this) { categories ->
            allCategories = categories
            updateUI()
        }

        viewModel.allExpenses.observe(this) { expenses ->
            allExpenses = expenses
            updateUI()
        }
    }

    private fun updateUI() {
        if (allCategories.isEmpty()) return

        val filteredExpenses = if (isDateFilterEnabled) {
            allExpenses.filter { 
                val expenseDate = resetTimeToStartOfDay(it.createdAt)
                expenseDate >= startDate && expenseDate <= endDate 
            }
        } else {
            allExpenses
        }

        // Summary Cards
        val totalSpending = filteredExpenses.sumOf { it.amount }
        val totalEntries = filteredExpenses.size

        binding.tvTotalSpending.text = String.format("$%.2f", totalSpending)
        binding.tvTotalEntries.text = totalEntries.toString()

        // Calculate Average Monthly Spending
        // Group by Year-Month
        if (filteredExpenses.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val monthlyGroups = filteredExpenses.groupBy { dateFormat.format(it.createdAt) }
            val avgMonthly = totalSpending / monthlyGroups.size
            binding.tvAvgSpending.text = String.format("$%.2f", avgMonthly)
        } else {
            binding.tvAvgSpending.text = "$0.00"
        }

        // Category Breakdown
        val categoryTotals = filteredExpenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val breakdownList = categoryTotals.entries.mapNotNull { (catId, amount) ->
            val category = allCategories.find { it.id == catId } ?: return@mapNotNull null
            CategoryBreakdown(category, amount, if (totalSpending > 0) (amount / totalSpending * 100) else 0.0)
        }.sortedByDescending { it.amount }

        breakdownAdapter.submitList(breakdownList)

        // Pie Chart
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        breakdownList.forEach { item ->
            entries.add(PieEntry(item.amount.toFloat(), item.category.name))
            colors.add(item.category.colorValue)
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "Categories")
            dataSet.colors = colors
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.WHITE
            
            val data = PieData(dataSet)
            binding.pieChart.data = data
            binding.pieChart.invalidate()
        } else {
            binding.pieChart.clear()
        }
    }
}

data class CategoryBreakdown(
    val category: Category,
    val amount: Double,
    val percentage: Double
)
