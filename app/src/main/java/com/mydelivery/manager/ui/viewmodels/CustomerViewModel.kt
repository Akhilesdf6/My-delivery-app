package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.model.CustomerDetails
import com.mydelivery.manager.data.repository.CustomerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    val customers: StateFlow<List<CustomerEntity>> = customerRepository.observeAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun loadCustomerDetails(
        customerId: Long,
        onResult: (CustomerDetails?) -> Unit
    ) {
        viewModelScope.launch {
            onResult(customerRepository.getCustomerDetails(customerId))
        }
    }


    fun updateCustomer(
        customer: CustomerEntity,
        name: String,
        phone: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val updated = customer.copy(
                name = name.trim().ifBlank { null },
                phone = phone.trim().ifBlank { null }
            )

            customerRepository.updateCustomer(updated)
            onResult("Customer Updated Successfully")
        }
    }

    fun deleteCustomer(
        customerId: Long,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val deleted = customerRepository.deleteCustomer(customerId)

            if (deleted) {
                onResult("Customer Deleted Successfully")
            } else {
                onResult("Cannot delete: customer has existing shipments")
            }
        }
    }

    fun addCustomer(name: String, phone: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = CustomerEntity(
                name = name.ifBlank { null },
                phone = phone.ifBlank { null },
                createdAt = now,
                updatedAt = now
            )
            customerRepository.addCustomer(entity)
            onResult("Customer Added Successfully")
        }
    }
}
