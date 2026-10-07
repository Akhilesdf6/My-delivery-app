package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.data.model.ShipmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    /**
     * Returns the new row id, or -1 if the insert was ignored because a shipment
     * with the same shipmentId already exists. Nothing is overwritten.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShipment(shipment: ShipmentEntity): Long

    @Update
    suspend fun updateShipment(shipment: ShipmentEntity): Int

    @Query("SELECT * FROM shipments WHERE id = :id")
    suspend fun getShipmentById(id: Long): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getShipmentByShipmentId(shipmentId: String): ShipmentEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM shipments WHERE shipmentId = :shipmentId)")
    suspend fun shipmentIdExists(shipmentId: String): Boolean

    @Query("SELECT * FROM shipments ORDER BY createdAt DESC, id DESC")
    suspend fun getAllShipments(): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE status = :status ORDER BY createdAt DESC, id DESC")
    suspend fun getShipmentsByStatus(status: ShipmentStatus): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE customerId = :customerId ORDER BY createdAt DESC, id DESC")
    suspend fun getShipmentsForCustomer(customerId: Long): List<ShipmentEntity>

    @Query("SELECT * FROM shipments ORDER BY createdAt DESC, id DESC")
    fun observeShipments(): Flow<List<ShipmentEntity>>

    /** Search preparation: [pattern] is an escaped LIKE pattern, e.g. "%123%". */
    @Query(
        "SELECT * FROM shipments WHERE shipmentId LIKE :pattern ESCAPE '\\' " +
            "ORDER BY createdAt DESC, id DESC",
    )
    suspend fun searchByShipmentId(pattern: String): List<ShipmentEntity>
}
