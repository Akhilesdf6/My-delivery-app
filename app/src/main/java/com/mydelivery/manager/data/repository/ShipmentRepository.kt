package com.mydelivery.manager.data.repository

import com.mydelivery.manager.data.local.dao.ShipmentDao
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow

sealed interface AddShipmentResult {
    data class Added(val rowId: Long) : AddShipmentResult

    /** A shipment with the same shipmentId already exists; nothing was saved. */
    data class Duplicate(val existing: ShipmentEntity?) : AddShipmentResult
}

class ShipmentRepository(
    private val dao: ShipmentDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /**
     * Saves a new shipment unless its shipmentId already exists.
     * Blank shipment IDs are stored as null (unknown); several unknown shipments are allowed.
     */
    suspend fun addShipment(shipment: ShipmentEntity): AddShipmentResult {
        require(shipment.codAmountPaise == null || shipment.codAmountPaise >= 0) {
            "COD amount cannot be negative"
        }
        val normalized = shipment.copy(shipmentId = shipment.shipmentId?.trim()?.takeIf { it.isNotEmpty() })
        val rowId = dao.insertShipment(normalized)
        return if (rowId == -1L) {
            AddShipmentResult.Duplicate(normalized.shipmentId?.let { dao.getShipmentByShipmentId(it) })
        } else {
            AddShipmentResult.Added(rowId)
        }
    }

    /** Low-level update only; delivery workflow rules belong to a later stage. */
    suspend fun updateShipment(shipment: ShipmentEntity) {
        dao.updateShipment(shipment.copy(updatedAt = clock()))
    }

    suspend fun getShipmentById(id: Long): ShipmentEntity? = dao.getShipmentById(id)

    suspend fun getShipmentByShipmentId(shipmentId: String): ShipmentEntity? =
        dao.getShipmentByShipmentId(shipmentId.trim())

    suspend fun shipmentIdExists(shipmentId: String): Boolean = dao.shipmentIdExists(shipmentId.trim())

    suspend fun getAllShipments(): List<ShipmentEntity> = dao.getAllShipments()

    suspend fun getShipmentsByStatus(status: ShipmentStatus): List<ShipmentEntity> =
        dao.getShipmentsByStatus(status)

    suspend fun getShipmentsForCustomer(customerId: Long): List<ShipmentEntity> =
        dao.getShipmentsForCustomer(customerId)

    fun observeShipments(): Flow<List<ShipmentEntity>> = dao.observeShipments()

    suspend fun searchByShipmentId(query: String): List<ShipmentEntity> =
        if (query.isBlank()) emptyList() else dao.searchByShipmentId(containsPattern(query))
}
