package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity): Int

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone ORDER BY id ASC LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC, id ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    /** [pattern] must already be a LIKE pattern escaped with backslash, e.g. "%raj%". */
    @Query(
        "SELECT * FROM customers " +
            "WHERE name LIKE :pattern ESCAPE '\\' OR phone LIKE :pattern ESCAPE '\\' " +
            "ORDER BY name COLLATE NOCASE ASC, id ASC",
    )
    suspend fun searchCustomers(pattern: String): List<CustomerEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM shipments WHERE customerId = :customerId)")
    suspend fun hasShipments(customerId: Long): Boolean

    @Query("DELETE FROM addresses WHERE customerId = :customerId")
    suspend fun deleteAddressesForCustomer(customerId: Long): Int

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: Long): Int
}
