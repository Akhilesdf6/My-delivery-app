package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.model.ShipmentStatus
import com.mydelivery.manager.data.repository.ShipmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalDeliveries: Int = 0,
    val pendingDeliveries: Int = 0,
    val deliveredCount: Int = 0
)

class HomeViewModel(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    // Converts the database Flow into a UI StateFlow
    val homeUiState: StateFlow<HomeUiState> = shipmentRepository.observeShipments().map { shipments ->
        val total = shipments.size
        val pending = shipments.count { 
            it.status == ShipmentStatus.PENDING || it.status == ShipmentStatus.OUT_FOR_DELIVERY 
        }
        val delivered = shipments.count { it.status == ShipmentStatus.DELIVERED }

        HomeUiState(
            totalDeliveries = total,
            pendingDeliveries = pending,
            deliveredCount = delivered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
