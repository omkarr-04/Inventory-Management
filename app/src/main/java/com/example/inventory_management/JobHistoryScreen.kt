package com.example.inventory_management

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobHistoryScreen(
    onJobClick: (JobCard) -> Unit,
    viewModel: JobHistoryViewModel = viewModel()
) {
    val jobs by viewModel.jobHistory.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job History") },
                actions = {
                    if (jobs.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No repair jobs found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobs) { job ->
                    JobHistoryCard(job = job, onClick = { onJobClick(job) })
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear History?") },
                text = { Text("This will permanently delete all repair records. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearHistory { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) showClearDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun JobHistoryCard(job: JobCard, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.vehicleNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customer: ${job.customerName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(job.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentStatusBadge(status = job.paymentStatus)
                    WorkStatusBadge(status = job.status)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatCurrency(job.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(status: String) {
    val (color, bgColor) = when (status) {
        "PAID" -> Color(0xFF388E3C) to Color(0xFFE8F5E9)
        "PARTIAL" -> Color(0xFFF57C00) to Color(0xFFFFF3E0)
        else -> Color.Red to Color(0xFFFFEBEE)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WorkStatusBadge(status: String) {
    val (color, bgColor) = when (status) {
        "PENDING" -> Color.Gray to Color(0xFFF5F5F5)
        "IN_PROGRESS" -> Color(0xFF1976D2) to Color(0xFFE3F2FD)
        "COMPLETED" -> Color(0xFF388E3C) to Color(0xFFE8F5E9)
        "DELIVERED" -> Color(0xFF1B5E20) to Color(0xFFC8E6C9)
        else -> Color.Gray to Color(0xFFF5F5F5)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    job: JobCard,
    onBack: () -> Unit,
    viewModel: JobHistoryViewModel = viewModel()
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val context = LocalContext.current
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusList = listOf("PENDING", "IN_PROGRESS", "COMPLETED", "DELIVERED")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { PdfHelper.generateAndShareInvoice(context, job) }) {
                        Icon(Icons.Default.Description, contentDescription = "Generate PDF")
                    }
                    Box {
                        IconButton(onClick = { showStatusMenu = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Change Status")
                        }
                        DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                            statusList.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        viewModel.updateStatus(job, status) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                showStatusMenu = false
                                                onBack()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (job.paymentStatus != "PAID") {
                        IconButton(onClick = { showAddPaymentDialog = true }) {
                            Icon(Icons.Default.Payments, contentDescription = "Update Payment")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = job.vehicleNumber, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            PaymentStatusBadge(status = job.paymentStatus)
                            WorkStatusBadge(status = job.status)
                        }
                    }
                    Text(text = "Customer: ${job.customerName}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Date: ${dateFormat.format(job.date)}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Description:", fontWeight = FontWeight.Bold)
                    Text(text = job.workDetails)
                }
            }

            Text(text = "Parts & Materials", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(job.partsUsed) { part ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = part.partName, fontWeight = FontWeight.Bold)
                                Text(text = "Qty: ${part.quantityUsed} x ${CurrencyUtils.formatCurrency(part.priceAtTime)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(text = CurrencyUtils.formatCurrency(part.quantityUsed * part.priceAtTime), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SummaryRow("Total Amount", job.totalAmount, isBold = true)
                    SummaryRow("Labor Charge", job.laborCharge)
                    SummaryRow("Amount Paid", job.amountPaid)
                    val balance = job.totalAmount - job.amountPaid
                    SummaryRow("Balance Due", balance, textColor = if (balance > 0) Color.Red else Color.Unspecified)
                }
            }
        }

        if (showAddPaymentDialog) {
            var additionalAmount by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddPaymentDialog = false },
                title = { Text("Update Payment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Vehicle: ${job.vehicleNumber}")
                        val balance = job.totalAmount - job.amountPaid
                        Text("Balance Due: ${CurrencyUtils.formatCurrency(balance)}", color = Color.Red, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = additionalAmount,
                            onValueChange = { additionalAmount = it },
                            label = { Text("Additional Payment (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amount = additionalAmount.toDoubleOrNull() ?: 0.0
                        viewModel.updatePayment(job, amount) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                showAddPaymentDialog = false
                                onBack()
                            }
                        }
                    }) { Text("Add Payment") }
                },
                dismissButton = { TextButton(onClick = { showAddPaymentDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double, isBold: Boolean = false, textColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            text = CurrencyUtils.formatCurrency(amount),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}
