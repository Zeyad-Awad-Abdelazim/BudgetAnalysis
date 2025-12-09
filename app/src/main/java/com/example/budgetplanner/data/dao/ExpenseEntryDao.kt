package com.example.budgetplanner.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.budgetplanner.data.entity.ExpenseEntry

@Dao
interface ExpenseEntryDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): LiveData<List<ExpenseEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntry)

    @Delete
    suspend fun delete(expense: ExpenseEntry)

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpenseCountByCategory(categoryId: String): Int
    
    @Query("SELECT * FROM expenses WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseEntry>>
}
