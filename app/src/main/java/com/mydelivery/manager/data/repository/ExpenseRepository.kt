package com.mydelivery.manager.data.repository

import com.mydelivery.manager.data.local.dao.ExpenseDao
import com.mydelivery.manager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun addExpense(expense: ExpenseEntity): Long {
        require(expense.amountPaise > 0) { "Expense amount must be greater than zero" }
        return dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        require(expense.amountPaise > 0) { "Expense amount must be greater than zero" }
        dao.updateExpense(expense)
    }

    /** [startInclusive, endExclusive) in epoch milliseconds. */
    suspend fun getExpensesByDateRange(startInclusive: Long, endExclusive: Long): List<ExpenseEntity> =
        dao.getExpensesByDateRange(startInclusive, endExclusive)

    fun observeExpenses(): Flow<List<ExpenseEntity>> = dao.observeExpenses()
}
