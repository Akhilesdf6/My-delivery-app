package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.data.repository.AddShipmentResult
import com.mydelivery.manager.data.repository.ShipmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeliveryViewModel(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {
    val shipments: StateFlow<List<ShipmentEntity>> = shipmentRepository.observeShipments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDelivery(shipmentId: String, codAmountRupees: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            // Convert Rupees to Paise
            val paise = codAmountRupees.toDoubleOrNull()?.let { (it * 100).toLong() }
            val now = System.currentTimeMillis()
            
            val entity = ShipmentEntity(
                shipmentId = shipmentId.ifBlank { null },
                codAmountPaise = paise,
                createdAt = now,
                updatedAt = now
            )
            
            val result = shipmentRepository.addShipment(entity)
            when (result) {
                is AddShipmentResult.Added -> onResult("Delivery Added Successfully")
                is AddShipmentResult.Duplicate -> onResult("Error: Duplicate Shipment ID")
            }
        }
    }
}
