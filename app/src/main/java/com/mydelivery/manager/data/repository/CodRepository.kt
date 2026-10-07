package com.mydelivery.manager.data.repository

import com.mydelivery.manager.data.local.dao.CodDao
import com.mydelivery.manager.data.local.entity.CodEntity

class CodRepository(
    private val dao: CodDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /** One COD record per shipment; a second one for the same shipment throws (unique index). */
    suspend fun addCod(cod: CodEntity): Long {
        require(cod.amountPaise >= 0) { "COD amount cannot be negative" }
        return dao.insertCod(cod)
    }

    suspend fun updateCod(cod: CodEntity) {
        require(cod.amountPaise >= 0) { "COD amount cannot be negative" }
        dao.updateCod(cod.copy(updatedAt = clock()))
    }

    suspend fun getCodForShipment(shipmentId: Long): CodEntity? = dao.getCodForShipment(shipmentId)

    suspend fun getPendingCod(): List<CodEntity> = dao.getPendingCod()

    suspend fun getCollectedCod(): List<CodEntity> = dao.getCollectedCod()

    suspend fun getDepositedCod(): List<CodEntity> = dao.getDepositedCod()
}
