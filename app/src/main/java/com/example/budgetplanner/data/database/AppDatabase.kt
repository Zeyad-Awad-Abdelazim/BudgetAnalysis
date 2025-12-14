package com.example.budgetplanner.data.database

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetplanner.data.dao.CategoryDao
import com.example.budgetplanner.data.dao.ExpenseEntryDao
import com.example.budgetplanner.data.entity.Category
import com.example.budgetplanner.data.entity.ExpenseEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [Category::class, ExpenseEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_planner_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val existingCategories = categoryDao.getAllCategoriesSync()
            if (existingCategories.isNotEmpty()) {
                return
            }
            
            categoryDao.insert(Category(UUID.randomUUID().toString(), "Food", "restaurant", Color.parseColor("#3F51B5")))
            categoryDao.insert(Category(UUID.randomUUID().toString(), "Transports", "bus", Color.parseColor("#7986CB")))
            categoryDao.insert(Category(UUID.randomUUID().toString(), "Bills", "receipt", Color.parseColor("#303F9F")))
            categoryDao.insert(Category(UUID.randomUUID().toString(), "Health", "heart", Color.parseColor("#E91E63")))
            categoryDao.insert(Category(UUID.randomUUID().toString(), "Entertainment", "celebration", Color.parseColor("#FFC107")))
        }
    }
}