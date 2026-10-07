package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydelivery.manager.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Insert
    suspend fun insertAddress(address: AddressEntity): Long

    @Update
    suspend fun updateAddress(address: AddressEntity): Int

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): AddressEntity?

    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY isPrimary DESC, id ASC")
    suspend fun getAddressesForCustomer(customerId: Long): List<AddressEntity>

    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY isPrimary DESC, id ASC")
    fun observeAddressesForCustomer(customerId: Long): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE pincode = :pincode ORDER BY id ASC")
    suspend fun getAddressesByPincode(pincode: String): List<AddressEntity>
}
