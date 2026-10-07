package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mydelivery.manager.data.model.IncomeType

/** Shipment -> zero/many income records. Money is Long paise, never Float/Double. */
@Entity(
    tableName = "income_records",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["shipmentId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("shipmentId"), Index("incomeDate")],
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: Long? = null,
    val incomeDate: Long,
    val type: IncomeType = IncomeType.DELIVERY_EARNING,
    val grossAmountPaise: Long,
    val tdsAmountPaise: Long,
    val netAmountPaise: Long,
    val note: String? = null,
    val createdAt: Long,
)
