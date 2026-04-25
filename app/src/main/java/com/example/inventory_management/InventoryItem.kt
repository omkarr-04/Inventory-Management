package com.example.inventory_management

import com.google.firebase.firestore.DocumentId

data class InventoryItem(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val lowStockThreshold: Int = 5 // Default threshold set to 5
)
