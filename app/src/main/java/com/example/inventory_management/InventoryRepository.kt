package com.example.inventory_management

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class InventoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val itemsCollection = firestore.collection("parts")

    // FETCH: Real-time updates from Firestore
    fun getItems(): Flow<List<InventoryItem>> {
        return itemsCollection.snapshots().map { snapshot ->
            snapshot.toObjects(InventoryItem::class.java)
        }
    }

    // SAVE: Add a new item
    suspend fun addItem(item: InventoryItem) {
        itemsCollection.add(item).await()
    }

    // DELETE: Remove item by ID
    suspend fun deleteItem(itemId: String) {
        itemsCollection.document(itemId).delete().await()
    }

    // UPDATE: Change quantity
    suspend fun updateQuantity(itemId: String, newQuantity: Int) {
        itemsCollection.document(itemId).update("quantity", newQuantity).await()
    }
}
