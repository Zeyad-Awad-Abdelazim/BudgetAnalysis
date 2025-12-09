package com.example.budgetplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetplanner.data.BudgetRepository
import com.example.budgetplanner.data.database.AppDatabase
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    val allCategories: LiveData<List<Category>>
    val allExpenses: LiveData<List<ExpenseEntry>>

    init {
        val categoryDao = AppDatabase.getDatabase(application, viewModelScope).categoryDao()
        val expenseEntryDao = AppDatabase.getDatabase(application, viewModelScope).expenseEntryDao()
        repository = BudgetRepository(categoryDao, expenseEntryDao)
        allCategories = repository.allCategories
        allExpenses = repository.allExpenses
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    fun deleteCategory(category: Category, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        val count = repository.getExpenseCountByCategory(category.id)
        if (count > 0) {
            onError("Cannot delete category with existing expenses.")
        } else {
            repository.deleteCategory(category)
            onSuccess()
        }
    }

    fun insertExpense(expense: ExpenseEntry) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun deleteExpense(expense: ExpenseEntry) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }
    
    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseEntry>> {
        return repository.getExpensesInRange(startDate, endDate)
    }
    
    suspend fun getCategoryById(id: String): Category? {
        return repository.getCategoryById(id)
    }
}
