package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.local.entity.CustomerEntity
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
