package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
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

    val shipments: StateFlow<List<ShipmentEntity>> =
        shipmentRepository.observeShipments()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addDelivery(
        shipmentId: String,
        codAmountRupees: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val paise = parseRupeesToPaise(codAmountRupees)

            if (codAmountRupees.isNotBlank() && paise == null) {
                onResult("Invalid COD amount")
                return@launch
            }

            val now = System.currentTimeMillis()

            val entity = ShipmentEntity(
                shipmentId = shipmentId.trim().takeIf { it.isNotEmpty() },
                codAmountPaise = paise,
                createdAt = now,
                updatedAt = now
            )

            when (val result = shipmentRepository.addShipment(entity)) {
                is AddShipmentResult.Added ->
                    onResult("Shipment saved locally")

                is AddShipmentResult.Duplicate ->
                    onResult("Duplicate Shipment ID - not saved")
            }
        }
    }

    fun addFullDelivery(
        shipmentId: String,
        customerName: String,
        phone: String,
        fullAddress: String,
        locality: String,
        pincode: String,
        codAmountRupees: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {

            val cleanShipmentId =
                shipmentId.trim().takeIf { it.isNotEmpty() }

            val cleanName =
                customerName.trim().takeIf { it.isNotEmpty() }

            val cleanPhone =
                phone.trim().takeIf { it.isNotEmpty() }

            val cleanAddress =
                fullAddress.trim().takeIf { it.isNotEmpty() }

            val cleanLocality =
                locality.trim().takeIf { it.isNotEmpty() }

            val cleanPincode =
                pincode.trim().takeIf { it.isNotEmpty() }

            val paise = parseRupeesToPaise(codAmountRupees)

            if (codAmountRupees.isNotBlank() && paise == null) {
                onResult("Invalid COD amount")
                return@launch
            }

            val now = System.currentTimeMillis()

            val customer = CustomerEntity(
                name = cleanName,
                phone = cleanPhone,
                createdAt = now,
                updatedAt = now
            )

            val address = AddressEntity(
                customerId = 0L,
                fullAddress = cleanAddress,
                locality = cleanLocality,
                pincode = cleanPincode,
                latitude = null,
                longitude = null,
                isPrimary = true,
                createdAt = now,
                updatedAt = now
            )

            val result = shipmentRepository.addFullShipment(
                shipmentId = cleanShipmentId,
                customer = customer,
                address = address,
                codAmountPaise = paise
            )

            onResult(result)
        }
    }

    private fun parseRupeesToPaise(
        value: String
    ): Long? {
        if (value.isBlank()) return null

        return try {
            val normalized = value.trim()
            val parts = normalized.split(".")

            if (parts.size > 2) return null

            val rupees = parts[0].ifBlank { "0" }.toLong()

            val decimal = if (parts.size == 2) {
                parts[1].padEnd(2, '0').take(2).toLong()
            } else {
                0L
            }

            rupees * 100L + decimal
        } catch (_: Exception) {
            null
        }
    }
}
