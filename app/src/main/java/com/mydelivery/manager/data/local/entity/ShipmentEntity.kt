package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mydelivery.manager.data.model.ShipmentStatus

/**
 * shipmentId is the label's shipment number (may be null if unreadable).
 * A UNIQUE index on it blocks duplicates; SQLite allows many NULLs.
 * Money is Long paise (Rs 30 = 3000).
 * RESTRICT on both links: history never vanishes because a customer/address is deleted.
 */
@Entity(
    tableName = "shipments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["id"],
            childColumns = ["addressId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["shipmentId"], unique = true),
        Index("customerId"),
        Index("addressId"),
        Index("status"),
    ],
)
data class ShipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: String? = null,
    val customerId: Long? = null,
    val addressId: Long? = null,
    val codAmountPaise: Long? = null,
    val status: ShipmentStatus = ShipmentStatus.PENDING,
    val undeliveredReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deliveredAt: Long? = null,
    val undeliveredAt: Long? = null,
)
