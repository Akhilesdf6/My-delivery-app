package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mydelivery.manager.data.model.CodStatus

/** Zero or one COD record per shipment (UNIQUE shipmentId). Money is Long paise. */
@Entity(
    tableName = "cod_records",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["shipmentId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["shipmentId"], unique = true), Index("status")],
)
data class CodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: Long,
    val amountPaise: Long,
    val status: CodStatus = CodStatus.PENDING,
    val collectedAt: Long? = null,
    val depositedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
