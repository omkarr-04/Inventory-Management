package com.example.inventory_management

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository()
    private val notificationHelper = NotificationHelper(application)

    // StateFlow to hold the list of items from Firestore
    val allItems: StateFlow<List<InventoryItem>> = repository.getItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        // Monitor stock levels in real-time for notifications
        viewModelScope.launch {
            allItems.collect { items ->
                items.forEach { item ->
                    if ((item.quantity > 0) && (item.quantity < item.lowStockThreshold)) {
                        notificationHelper.showLowStockNotification(item.name, item.quantity)
                    }
                }
            }
        }
    }

    fun addItem(
        name: String,
        category: String,
        quantityStr: String,
        priceStr: String,
        thresholdStr: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (name.isBlank() || category.isBlank() || quantityStr.isBlank() || priceStr.isBlank()) {
            onResult(false, "Please fill in all fields")
            return
        }

        val quantity = quantityStr.toIntOrNull()
        val price = priceStr.toDoubleOrNull()
        val threshold = thresholdStr.toIntOrNull() ?: 5

        if (quantity == null || quantity < 0) {
            onResult(false, "Invalid quantity")
            return
        }

        if (price == null || price < 0) {
            onResult(false, "Invalid price")
            return
        }

        val newItem = InventoryItem(
            name = name,
            category = category,
            quantity = quantity,
            price = price,
            lowStockThreshold = threshold
        )

        viewModelScope.launch {
            try {
                repository.addItem(newItem)
                onResult(true, "Item added successfully!")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item.id)
            } catch (_: Exception) { }
        }
    }

    fun updateQuantity(item: InventoryItem, delta: Int) {
        val newQuantity = item.quantity + delta
        if (newQuantity >= 0) {
            viewModelScope.launch {
                try {
                    repository.updateQuantity(item.id, newQuantity)
                } catch (_: Exception) { }
            }
        }
    }
}
