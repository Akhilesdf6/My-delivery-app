package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.CodEntity

@Dao
interface CodDao {
    @Insert
    suspend fun insertCod(cod: CodEntity): Long

    @Update
    suspend fun updateCod(cod: CodEntity): Int

    @Query("SELECT * FROM cod_records WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getCodForShipment(shipmentId: Long): CodEntity?

    // The literals below are the CodStatus names (see Converters); a test guards them.
    @Query("SELECT * FROM cod_records WHERE status = 'PENDING' ORDER BY createdAt ASC, id ASC")
    suspend fun getPendingCod(): List<CodEntity>

    @Query("SELECT * FROM cod_records WHERE status = 'COLLECTED' ORDER BY createdAt ASC, id ASC")
    suspend fun getCollectedCod(): List<CodEntity>

    @Query("SELECT * FROM cod_records WHERE status = 'DEPOSITED' ORDER BY createdAt ASC, id ASC")
    suspend fun getDepositedCod(): List<CodEntity>
}
