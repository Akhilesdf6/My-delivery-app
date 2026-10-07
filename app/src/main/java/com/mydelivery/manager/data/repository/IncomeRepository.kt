package com.mydelivery.manager.data.repository

import com.mydelivery.manager.data.local.dao.IncomeDao
import com.mydelivery.manager.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val dao: IncomeDao) {
    /** Amounts are Long paise. Throws IllegalArgumentException if the amounts don't add up. */
    suspend fun addIncome(income: IncomeEntity): Long {
        validate(income)
        return dao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        validate(income)
        dao.updateIncome(income)
    }

    suspend fun getIncomeForShipment(shipmentId: Long): List<IncomeEntity> = dao.getIncomeForShipment(shipmentId)

    /** [startInclusive, endExclusive) in epoch milliseconds. */
    suspend fun getIncomeByDateRange(startInclusive: Long, endExclusive: Long): List<IncomeEntity> =
        dao.getIncomeByDateRange(startInclusive, endExclusive)

    fun observeIncome(): Flow<List<IncomeEntity>> = dao.observeIncome()

    private fun validate(income: IncomeEntity) {
        require(income.grossAmountPaise >= 0) { "Gross amount cannot be negative" }
        require(income.tdsAmountPaise >= 0) { "TDS cannot be negative" }
        require(income.tdsAmountPaise <= income.grossAmountPaise) { "TDS cannot exceed gross" }
        require(income.netAmountPaise == income.grossAmountPaise - income.tdsAmountPaise) {
            "Net must equal gross minus TDS"
        }
    }
}
