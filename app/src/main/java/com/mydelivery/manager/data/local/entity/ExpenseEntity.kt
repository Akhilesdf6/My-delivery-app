package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mydelivery.manager.data.model.ExpenseCategory

/** Money is Long paise. */
@Entity(tableName = "expenses", indices = [Index("expenseDate")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long,
    val category: ExpenseCategory,
    val note: String? = null,
    val expenseDate: Long,
    val createdAt: Long,
)
