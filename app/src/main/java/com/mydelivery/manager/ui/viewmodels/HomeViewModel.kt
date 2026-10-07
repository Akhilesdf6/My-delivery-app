package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.model.ShipmentStatus
import com.mydelivery.manager.data.repository.ExpenseRepository
import com.mydelivery.manager.data.repository.IncomeRepository
import com.mydelivery.manager.data.repository.ShipmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalDeliveries: Int = 0,
    val pendingDeliveries: Int = 0,
    val deliveredCount: Int = 0,
    val incomePaise: Long = 0L,
    val expensePaise: Long = 0L,
    val netPaise: Long = 0L
)

class HomeViewModel(
    shipmentRepository: ShipmentRepository,
    incomeRepository: IncomeRepository,
    expenseRepository: ExpenseRepository
) : ViewModel() {

    private val shipmentsFlow = shipmentRepository.observeShipments()

    private val incomeFlow = incomeRepository.observeIncome()

    private val expenseFlow = expenseRepository.observeExpenses()

    val homeUiState: StateFlow<HomeUiState> =
        combine(
            shipmentsFlow,
            incomeFlow,
            expenseFlow
        ) { shipments, incomes, expenses ->

            val total = shipments.size

            val pending = shipments.count {
                it.status == ShipmentStatus.PENDING ||
                    it.status == ShipmentStatus.OUT_FOR_DELIVERY
            }

            val delivered = shipments.count {
                it.status == ShipmentStatus.DELIVERED
            }

            val income = incomes.sumOf { it.netAmountPaise }
            val expense = expenses.sumOf { it.amountPaise }

            HomeUiState(
                totalDeliveries = total,
                pendingDeliveries = pending,
                deliveredCount = delivered,
                incomePaise = income,
                expensePaise = expense,
                netPaise = income - expense
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
}
