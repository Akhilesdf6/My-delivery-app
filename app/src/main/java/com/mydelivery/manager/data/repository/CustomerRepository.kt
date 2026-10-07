package com.mydelivery.manager.data.repository

import com.mydelivery.manager.data.local.dao.AddressDao
import com.mydelivery.manager.data.local.dao.CustomerDao
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/** Customers and their addresses. Addresses are only ever added or edited explicitly, never overwritten silently. */
class CustomerRepository(
    private val customerDao: CustomerDao,
    private val addressDao: AddressDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun getCustomerByPhone(phone: String): CustomerEntity? = customerDao.getCustomerByPhone(phone.trim())

    /** Searches name and phone. A blank query returns an empty list. */
    suspend fun searchCustomers(query: String): List<CustomerEntity> =
        if (query.isBlank()) emptyList() else customerDao.searchCustomers(containsPattern(query))

    /** Returns the new customer id. */
    suspend fun addCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer.copy(updatedAt = clock()))
    }

    // ---- addresses ----

    suspend fun addAddress(address: AddressEntity): Long = addressDao.insertAddress(address)

    suspend fun updateAddress(address: AddressEntity) {
        addressDao.updateAddress(address.copy(updatedAt = clock()))
    }

    suspend fun getAddressById(id: Long): AddressEntity? = addressDao.getAddressById(id)

    suspend fun getAddressesForCustomer(customerId: Long): List<AddressEntity> =
        addressDao.getAddressesForCustomer(customerId)

    fun observeAddressesForCustomer(customerId: Long): Flow<List<AddressEntity>> =
        addressDao.observeAddressesForCustomer(customerId)

    suspend fun getAddressesByPincode(pincode: String): List<AddressEntity> =
        addressDao.getAddressesByPincode(pincode.trim())
}
