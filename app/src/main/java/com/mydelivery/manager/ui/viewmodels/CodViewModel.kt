package com.mydelivery.manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.repository.CodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CodUiState(
    val pending: List<CodEntity> = emptyList(),
    val collected: List<CodEntity> = emptyList(),
    val deposited: List<CodEntity> = emptyList()
)

class CodViewModel(
    private val codRepository: CodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodUiState())
    val uiState: StateFlow<CodUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val pending = codRepository.getPendingCod()
            val collected = codRepository.getCollectedCod()
            val deposited = codRepository.getDepositedCod()
            
            _uiState.value = CodUiState(
                pending = pending,
                collected = collected,
                deposited = deposited
            )
        }
    }
}
