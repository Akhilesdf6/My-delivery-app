package com.mydelivery.manager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.ui.utils.AppViewModelProvider
import com.mydelivery.manager.ui.viewmodels.CodViewModel

@Composable
fun CodScreen(
    modifier: Modifier = Modifier,
    viewModel: CodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh data every time the screen is opened
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Cash on Delivery",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { SectionHeader("Pending COD") }
                if (uiState.pending.isEmpty()) {
                    item { EmptyText("No pending COD") }
                } else {
                    items(uiState.pending, key = { it.id }) { cod -> CodItem(cod) }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { SectionHeader("Collected COD") }
                if (uiState.collected.isEmpty()) {
                    item { EmptyText("No collected COD") }
                } else {
                    items(uiState.collected, key = { it.id }) { cod -> CodItem(cod) }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { SectionHeader("Deposited COD") }
                if (uiState.deposited.isEmpty()) {
                    item { EmptyText("No deposited COD") }
                } else {
                    items(uiState.deposited, key = { it.id }) { cod -> CodItem(cod) }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun EmptyText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CodItem(cod: CodEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Shipment DB ID: ${cod.shipmentId}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹ ${cod.amountPaise / 100.0}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
