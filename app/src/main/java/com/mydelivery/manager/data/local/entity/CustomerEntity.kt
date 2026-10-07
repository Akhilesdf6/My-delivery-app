package com.mydelivery.manager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Name and phone may be missing. Missing stays null; nothing is ever invented. */
@Entity(tableName = "customers", indices = [Index("phone")])
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String? = null,
    val phone: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
