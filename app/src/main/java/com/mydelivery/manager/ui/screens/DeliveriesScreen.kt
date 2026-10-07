package com.mydelivery.manager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.ui.utils.AppViewModelProvider
import com.mydelivery.manager.ui.viewmodels.DeliveryViewModel

@Composable
fun DeliveriesScreen(
    modifier: Modifier = Modifier,
    viewModel: DeliveryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val shipments by viewModel.shipments.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Delivery")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Deliveries",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (shipments.isEmpty()) {
                Text(
                    text = "No deliveries found. Click + to add.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shipments, key = { it.id }) { shipment ->
                        ShipmentItem(shipment = shipment)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeliveryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { trackingId, codAmount ->
                viewModel.addDelivery(trackingId, codAmount) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddDeliveryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var trackingId by remember { mutableStateOf("") }
    var codAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Delivery") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = trackingId,
                    onValueChange = { trackingId = it },
                    label = { Text("Shipment ID (Optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = codAmount,
                    onValueChange = { codAmount = it },
                    label = { Text("COD Amount ₹ (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trackingId, codAmount) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ShipmentItem(shipment: ShipmentEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${shipment.shipmentId ?: "Not Assigned"}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${shipment.status.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (shipment.codAmountPaise != null && shipment.codAmountPaise > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "COD: ₹${shipment.codAmountPaise / 100.0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
