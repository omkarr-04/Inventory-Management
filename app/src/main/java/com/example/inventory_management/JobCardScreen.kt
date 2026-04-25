package com.example.inventory_management

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardScreen(
    onJobSaved: () -> Unit,
    viewModel: JobViewModel = viewModel()
) {
    val context = LocalContext.current
    val availableParts by viewModel.availableParts.collectAsStateWithLifecycle()
    val selectedParts by viewModel.selectedParts.collectAsStateWithLifecycle()

    var vehicleNumber by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var workDetails by remember { mutableStateOf("") }
    var amountPaid by remember { mutableStateOf("") }
    var laborCharge by remember { mutableStateOf("") }
    var gstPercentage by remember { mutableStateOf("0") }
    
    val partsSubtotal = selectedParts.sumOf { it.priceAtTime * it.quantityUsed }
    val subtotalWithLabor = partsSubtotal + (laborCharge.toDoubleOrNull() ?: 0.0)
    val gstAmount = subtotalWithLabor * ((gstPercentage.toDoubleOrNull() ?: 0.0) / 100.0)
    val totalAmount = subtotalWithLabor + gstAmount
    
    var showPartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Job Card") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = workDetails,
                        onValueChange = { workDetails = it },
                        label = { Text("Work Details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parts Used", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { showPartDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Part")
                        }
                    }
                }

                items(selectedParts) { part ->
                    val partTotal = part.priceAtTime * part.quantityUsed
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(part.partName, fontWeight = FontWeight.Bold)
                                Text("Qty: ${part.quantityUsed} x ${CurrencyUtils.formatCurrency(part.priceAtTime)}")
                                Text("Total: ${CurrencyUtils.formatCurrency(partTotal)}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.removePartFromJob(part) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Charges & Tax", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = laborCharge,
                            onValueChange = { laborCharge = it },
                            label = { Text("Labor Charge (₹)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gstPercentage,
                            onValueChange = { gstPercentage = it },
                            label = { Text("GST %") },
                            modifier = Modifier.weight(0.6f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                item {
                    // Final Bill Summary Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryLine("Parts Subtotal", partsSubtotal)
                            SummaryLine("Labor Charge", laborCharge.toDoubleOrNull() ?: 0.0)
                            SummaryLine("GST Amount", gstAmount)
                            Divider()
                            Text(
                                text = "Total Bill: ${CurrencyUtils.formatCurrency(totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = amountPaid,
                                onValueChange = { amountPaid = it },
                                label = { Text("Amount Paid (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            val status = JobCard.calculateStatus(totalAmount, amountPaid.toDoubleOrNull() ?: 0.0)
                            Text(
                                text = "Payment Status: $status",
                                fontWeight = FontWeight.Bold,
                                color = when(status) {
                                    "PAID" -> Color(0xFF388E3C)
                                    "PARTIAL" -> Color(0xFFF57C00)
                                    else -> Color.Red
                                }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.submitJob(vehicleNumber, customerName, workDetails, amountPaid, laborCharge, gstPercentage) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onJobSaved()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Save Job & Update Stock")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showPartDialog) {
        SelectPartDialog(
            parts = availableParts,
            onDismiss = { showPartDialog = false },
            onPartSelected = { item, qty ->
                viewModel.addPartToJob(item, qty) { success, message ->
                    if (!success) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        showPartDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun SummaryLine(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(CurrencyUtils.formatCurrency(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SelectPartDialog(
    parts: List<InventoryItem>,
    onDismiss: () -> Unit,
    onPartSelected: (InventoryItem, Int) -> Unit
) {
    var selectedPart by remember { mutableStateOf<InventoryItem?>(null) }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Part from Inventory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose a part:", style = MaterialTheme.typography.labelLarge)
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(parts) { part ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPart = part },
                            color = if (selectedPart?.id == part.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${part.name} (${CurrencyUtils.formatCurrency(part.price)})",
                                    color = if (selectedPart?.id == part.id) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity to Use") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                selectedPart?.let { 
                    val q = quantity.toIntOrNull() ?: 0
                    if (q > 0) {
                        onPartSelected(it, q)
                    }
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
