package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Customer 1 -> many addresses.
 * RESTRICT: a customer that still has addresses cannot be deleted by accident.
 */
@Entity(
    tableName = "addresses",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("customerId"), Index("pincode"), Index("locality")],
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val fullAddress: String? = null,
    val locality: String? = null,
    val pincode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isPrimary: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
