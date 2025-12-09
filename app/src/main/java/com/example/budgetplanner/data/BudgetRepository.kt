package com.example.budgetplanner.data

import androidx.lifecycle.LiveData
import com.example.budgetplanner.data.dao.CategoryDao
import com.example.budgetplanner.data.dao.ExpenseEntryDao
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry

class BudgetRepository(private val categoryDao: CategoryDao, private val expenseEntryDao: ExpenseEntryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    val allExpenses: LiveData<List<ExpenseEntry>> = expenseEntryDao.getAllExpenses()

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
    
    suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertExpense(expense: ExpenseEntry) {
        expenseEntryDao.insert(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntry) {
        expenseEntryDao.delete(expense)
    }
    
    suspend fun getExpenseCountByCategory(categoryId: String): Int {
        return expenseEntryDao.getExpenseCountByCategory(categoryId)
    }
    
    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseEntry>> {
        return expenseEntryDao.getExpensesInRange(startDate, endDate)
    }
}
