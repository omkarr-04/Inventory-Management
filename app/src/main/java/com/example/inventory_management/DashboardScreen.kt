package com.example.inventory_management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToInventory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel(),
    historyViewModel: JobHistoryViewModel = viewModel(),
) {
    val items by inventoryViewModel.allItems.collectAsStateWithLifecycle()
    val jobs by historyViewModel.jobHistory.collectAsStateWithLifecycle()

    // Dashboard Calculations
    val totalParts = items.size
    val lowStockCount = items.count { it.quantity < 5 }
    
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val todayJobs = jobs.filter { it.date.after(today) }
    val todayRevenue = todayJobs.sumOf { it.amountPaid }
    val pendingPayments = jobs.sumOf { it.totalAmount - it.amountPaid }

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text("Shop Dashboard", fontWeight = FontWeight.Bold) })
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StatCard(
                    title = "Revenue Today",
                    value = CurrencyUtils.formatCurrency(todayRevenue),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToHistory,
                )
            }
            item {
                StatCard(
                    title = "Pending Dues",
                    value = CurrencyUtils.formatCurrency(pendingPayments),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFD32F2F),
                    onClick = onNavigateToHistory,
                )
            }
            item {
                StatCard(
                    title = "Inventory Items",
                    value = totalParts.toString(),
                    icon = Icons.Default.Inventory2,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToInventory,
                )
            }
            item {
                StatCard(
                    title = "Low Stock",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = if (lowStockCount > 0) Color(0xFFF57C00) else Color.Gray,
                    onClick = onNavigateToInventory,
                )
            }
            item {
                StatCard(
                    title = "Jobs Today",
                    value = todayJobs.size.toString(),
                    icon = Icons.Default.Build,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToHistory,
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
