package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity): Int

    @Query("SELECT * FROM income_records WHERE shipmentId = :shipmentId ORDER BY incomeDate ASC, id ASC")
    suspend fun getIncomeForShipment(shipmentId: Long): List<IncomeEntity>

    /** Range is [startInclusive, endExclusive) in epoch milliseconds. */
    @Query(
        "SELECT * FROM income_records " +
            "WHERE incomeDate >= :startInclusive AND incomeDate < :endExclusive " +
            "ORDER BY incomeDate ASC, id ASC",
    )
    suspend fun getIncomeByDateRange(startInclusive: Long, endExclusive: Long): List<IncomeEntity>

    @Query("SELECT * FROM income_records ORDER BY incomeDate DESC, id DESC")
    fun observeIncome(): Flow<List<IncomeEntity>>
}
