package com.mydelivery.manager.data.repository

import androidx.room.withTransaction
import com.mydelivery.manager.data.local.AppDatabase
import com.mydelivery.manager.data.local.dao.AddressDao
import com.mydelivery.manager.data.local.dao.CodDao
import com.mydelivery.manager.data.local.dao.DeliveryPhotoDao
import com.mydelivery.manager.data.local.dao.CustomerDao
import com.mydelivery.manager.data.local.dao.ShipmentDao
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.local.entity.DeliveryPhotoEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.data.model.CodStatus
import com.mydelivery.manager.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow

sealed interface AddShipmentResult {
    data class Added(val rowId: Long) : AddShipmentResult
    data class Duplicate(val existing: ShipmentEntity?) : AddShipmentResult
}

class ShipmentRepository(
    private val dao: ShipmentDao,
    private val customerDao: CustomerDao,
    private val addressDao: AddressDao,
    private val codDao: CodDao,
    private val deliveryPhotoDao: DeliveryPhotoDao,
    private val database: AppDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun addShipment(shipment: ShipmentEntity): AddShipmentResult {
        require(shipment.codAmountPaise == null || shipment.codAmountPaise >= 0) {
            "COD amount cannot be negative"
        }

        val normalized = shipment.copy(
            shipmentId = shipment.shipmentId
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        )

        val rowId = dao.insertShipment(normalized)

        return if (rowId == -1L) {
            AddShipmentResult.Duplicate(
                normalized.shipmentId?.let {
                    dao.getShipmentByShipmentId(it)
                }
            )
        } else {
            AddShipmentResult.Added(rowId)
        }
    }

    suspend fun addFullShipment(
        shipmentId: String?,
        customer: CustomerEntity,
        address: AddressEntity,
        codAmountPaise: Long?,
    ): Long {

        require(codAmountPaise == null || codAmountPaise >= 0) {
            "COD amount cannot be negative"
        }

        val cleanShipmentId = shipmentId
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (cleanShipmentId != null && dao.shipmentIdExists(cleanShipmentId)) {
            error("Duplicate Shipment ID")
        }

        val now = clock()
        var createdShipmentId = -1L

        database.withTransaction {

            val customerId = customerDao.insertCustomer(
                customer.copy(
                    createdAt = now,
                    updatedAt = now
                )
            )

            val addressId = addressDao.insertAddress(
                address.copy(
                    customerId = customerId,
                    createdAt = now,
                    updatedAt = now
                )
            )

            val shipmentRowId = dao.insertShipment(
                ShipmentEntity(
                    shipmentId = cleanShipmentId,
                    customerId = customerId,
                    addressId = addressId,
                    codAmountPaise = codAmountPaise,
                    status = ShipmentStatus.PENDING,
                    createdAt = now,
                    updatedAt = now
                )
            )

            if (shipmentRowId == -1L) {
                error("Duplicate Shipment ID")
            }

            createdShipmentId = shipmentRowId

            if (codAmountPaise != null && codAmountPaise > 0) {
                codDao.insertCod(
                    CodEntity(
                        shipmentId = shipmentRowId,
                        amountPaise = codAmountPaise,
                        status = CodStatus.PENDING,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }

        return createdShipmentId
    }

    suspend fun saveDeliveryPhoto(
        shipmentId: Long,
        uri: String,
    ): Long {
        return deliveryPhotoDao.insertPhoto(
            DeliveryPhotoEntity(
                shipmentId = shipmentId,
                uri = uri,
                createdAt = clock(),
            )
        )
    }

    suspend fun updateShipment(shipment: ShipmentEntity) {
        dao.updateShipment(
            shipment.copy(updatedAt = clock())
        )
    }


    suspend fun markDelivered(
        shipmentId: Long
    ): String {

        val now = clock()

        database.withTransaction {

            val shipment = dao.getShipmentById(shipmentId)
                ?: error("Shipment not found")

            val updatedShipment = shipment.copy(
                status = ShipmentStatus.DELIVERED,
                undeliveredReason = null,
                deliveredAt = now,
                undeliveredAt = null,
                updatedAt = now
            )

            val updated = dao.updateShipment(updatedShipment)

            if (updated <= 0) {
                error("Unable to update shipment")
            }

            val codAmount = shipment.codAmountPaise ?: 0L

            if (codAmount > 0) {

                val existingCod = codDao.getCodForShipment(shipmentId)

                if (existingCod != null) {

                    codDao.updateCod(
                        existingCod.copy(
                            status = CodStatus.COLLECTED,
                            collectedAt = now,
                            updatedAt = now
                        )
                    )

                } else {

                    codDao.insertCod(
                        CodEntity(
                            shipmentId = shipmentId,
                            amountPaise = codAmount,
                            status = CodStatus.COLLECTED,
                            collectedAt = now,
                            depositedAt = null,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
            }
        }

        return "Delivery marked successfully"
    }

    suspend fun markUndelivered(
        shipmentId: Long,
        reason: String
    ): String {

        val cleanReason = reason.trim()

        if (cleanReason.isBlank()) {
            return "Undelivered reason is required"
        }

        val now = clock()

        database.withTransaction {

            val shipment = dao.getShipmentById(shipmentId)
                ?: error("Shipment not found")

            val updatedShipment = shipment.copy(
                status = ShipmentStatus.UNDELIVERED,
                undeliveredReason = cleanReason,
                deliveredAt = null,
                undeliveredAt = now,
                updatedAt = now
            )

            val updated = dao.updateShipment(updatedShipment)

            if (updated <= 0) {
                error("Unable to update shipment")
            }
        }

        return "Shipment marked as undelivered"
    }

    suspend fun getShipmentById(id: Long): ShipmentEntity? =
        dao.getShipmentById(id)

    suspend fun getShipmentByShipmentId(
        shipmentId: String
    ): ShipmentEntity? =
        dao.getShipmentByShipmentId(shipmentId.trim())

    suspend fun shipmentIdExists(
        shipmentId: String
    ): Boolean =
        dao.shipmentIdExists(shipmentId.trim())

    suspend fun getAllShipments(): List<ShipmentEntity> =
        dao.getAllShipments()

    suspend fun getShipmentsByStatus(
        status: ShipmentStatus
    ): List<ShipmentEntity> =
        dao.getShipmentsByStatus(status)

    suspend fun getShipmentsForCustomer(
        customerId: Long
    ): List<ShipmentEntity> =
        dao.getShipmentsForCustomer(customerId)

    fun observeShipments(): Flow<List<ShipmentEntity>> =
        dao.observeShipments()

    suspend fun searchByShipmentId(
        query: String
    ): List<ShipmentEntity> =
        if (query.isBlank()) {
            emptyList()
        } else {
            dao.searchByShipmentId(
                containsPattern(query)
            )
        }
}
