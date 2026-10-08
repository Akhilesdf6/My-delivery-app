package com.mydelivery.manager.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.mydelivery.manager.utils.extractTextFromImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun DeliveriesScreen(
    modifier: Modifier = Modifier,
    viewModel: DeliveryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val shipments by viewModel.shipments.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filtered = remember(shipments, search) {
        if (search.isBlank()) {
            shipments
        } else {
            shipments.filter {
                (it.shipmentId ?: "").contains(search, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Shipment"
                )
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
                text = "Shipments",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search Shipment ID") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Text(
                    text = if (search.isBlank())
                        "No shipments yet. Tap + to add."
                    else
                        "No matching shipment found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        filtered,
                        key = { it.id }
                    ) { shipment ->
                        ShipmentItem(shipment)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddShipmentDialog(
            onDismiss = {
                showAddDialog = false
            },
            onConfirm = { shipmentId, name, phone, address, locality, pincode, cod, labelUri ->

                viewModel.addFullDelivery(
                    shipmentId = shipmentId,
                    customerName = name,
                    phone = phone,
                    fullAddress = address,
                    locality = locality,
                    pincode = pincode,
                    codAmountRupees = cod,
                    labelUri = labelUri
                ) { message ->
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddShipmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String?
    ) -> Unit
) {
    val context = LocalContext.current

    var shipmentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var cod by remember { mutableStateOf("") }
    var labelUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val labelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        labelUri = uri
    }

    LaunchedEffect(labelUri) {
        val uri = labelUri ?: return@LaunchedEffect

        isScanning = true

        try {
            val result = extractTextFromImage(
                context = context,
                uri = uri
            )

            result.possibleShipmentId?.let {
                shipmentId = it
            }

            result.possibleName?.let {
                name = it
            }

            result.possiblePhone?.let {
                phone = it
            }

            result.possiblePincode?.let {
                pincode = it
            }

            result.possibleCodAmount?.let {
                cod = it
            }

            if (result.rawAddressText.isNotBlank()) {
                address = result.rawAddressText
            }

        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Label scan failed: ${e.message ?: "Unknown error"}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } finally {
            isScanning = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Shipment")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    androidx.compose.material3.Button(
                        onClick = {
                            labelPicker.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                isScanning -> "🔍 Reading Label..."
                                labelUri == null -> "📸 Upload Label"
                                else -> "✅ Label Scanned"
                            }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = shipmentId,
                        onValueChange = { shipmentId = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Shipment ID") },
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer Name") },
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer Address") },
                        minLines = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Locality") },
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pincode") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = cod,
                        onValueChange = { cod = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("COD Amount ₹") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unknown information can be left blank. Nothing will be invented.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        shipmentId,
                        name,
                        phone,
                        address,
                        locality,
                        pincode,
                        cod,
                        labelUri?.toString()
                    )
                }
            ) {
                Text("Save Shipment")
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
private fun ShipmentItem(
    shipment: ShipmentEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = shipment.shipmentId
                    ?.takeIf { it.isNotBlank() }
                    ?: "Shipment ID: Unknown",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Status: ${shipment.status.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            shipment.codAmountPaise?.let { paise ->
                if (paise > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "COD: ₹%.2f".format(paise / 100.0),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (shipment.deliveredAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Delivered")
            }

            shipment.undeliveredReason?.let { reason ->
                if (reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Reason: $reason")
                }
            }
        }
    }
}
