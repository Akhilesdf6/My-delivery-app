package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity): Int

    /** Range is [startInclusive, endExclusive) in epoch milliseconds. */
    @Query(
        "SELECT * FROM expenses " +
            "WHERE expenseDate >= :startInclusive AND expenseDate < :endExclusive " +
            "ORDER BY expenseDate ASC, id ASC",
    )
    suspend fun getExpensesByDateRange(startInclusive: Long, endExclusive: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, id DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>
}
