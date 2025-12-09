package com.example.budgetplanner.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class ExpenseEntry(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val categoryId: String,
    val createdAt: Long
)
