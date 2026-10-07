package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "delivery_photos",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["shipmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shipmentId")],
)
data class DeliveryPhotoEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shipmentId: Long,
    val uri: String,
    val createdAt: Long,
)
