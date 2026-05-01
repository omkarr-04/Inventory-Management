package com.example.inventory_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobViewModel : ViewModel() {
    private val jobRepository = JobRepository()
    private val inventoryRepository = InventoryRepository()

    private val _availableParts = MutableStateFlow<List<InventoryItem>>(emptyList())
    val availableParts: StateFlow<List<InventoryItem>> = _availableParts.asStateFlow()

    private val _selectedParts = MutableStateFlow<List<UsedPart>>(emptyList())
    val selectedParts: StateFlow<List<UsedPart>> = _selectedParts.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getItems().collect {
                _availableParts.value = it
            }
        }
    }

    fun addPartToJob(item: InventoryItem, quantityToAdd: Int, onResult: (Boolean, String) -> Unit) {
        val currentList = _selectedParts.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.partId == item.id }
        
        val currentUsedQty = if (existingIndex != -1) currentList[existingIndex].quantityUsed else 0
        val totalRequestedQty = currentUsedQty + quantityToAdd

        if (totalRequestedQty > item.quantity) {
            onResult(false, "Cannot add $quantityToAdd. Only ${item.quantity - currentUsedQty} more available.")
            return
        }

        if (existingIndex != -1) {
            currentList[existingIndex] = currentList[existingIndex].copy(quantityUsed = totalRequestedQty)
        } else {
            currentList.add(
                UsedPart(
                    partId = item.id,
                    partName = item.name,
                    quantityUsed = quantityToAdd,
                    priceAtTime = item.price,
                ),
            )
        }
        
        _selectedParts.value = currentList
        onResult(true, "${item.name} added.")
    }

    fun removePartFromJob(usedPart: UsedPart) {
        _selectedParts.value = _selectedParts.value.filter { it.partId != usedPart.partId }
    }

    fun submitJob(
        vehicleNumber: String,
        customerName: String,
        workDetails: String,
        amountPaidStr: String,
        laborChargeStr: String,
        gstPercentageStr: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        val currentParts = _selectedParts.value
        if (vehicleNumber.isBlank() || customerName.isBlank() || workDetails.isBlank() || currentParts.isEmpty()) {
            onResult(false, "Please fill all fields and add parts.")
            return
        }

        val laborCharge = laborChargeStr.toDoubleOrNull() ?: 0.0
        val gstPercentage = gstPercentageStr.toDoubleOrNull() ?: 0.0
        
        val partsSubtotal = currentParts.sumOf { it.priceAtTime * it.quantityUsed }
        val subtotalWithLabor = partsSubtotal + laborCharge
        val gstAmount = subtotalWithLabor * (gstPercentage / 100.0)
        val totalAmount = subtotalWithLabor + gstAmount
        
        val amountPaid = amountPaidStr.toDoubleOrNull() ?: 0.0

        if (amountPaid > totalAmount) {
            onResult(false, "Paid amount cannot exceed total amount.")
            return
        }
        
        if (amountPaid < 0) {
            onResult(false, "Paid amount cannot be negative.")
            return
        }

        val paymentStatus = JobCard.calculateStatus(totalAmount, amountPaid)
        val invoiceNo = "INV-${System.currentTimeMillis().toString().takeLast(6)}"

        viewModelScope.launch {
            try {
                val job = JobCard(
                    invoiceNumber = invoiceNo,
                    vehicleNumber = vehicleNumber,
                    customerName = customerName,
                    workDetails = workDetails,
                    partsUsed = currentParts,
                    laborCharge = laborCharge,
                    gstPercentage = gstPercentage,
                    totalAmount = totalAmount,
                    amountPaid = amountPaid,
                    paymentStatus = paymentStatus,
                    status = "PENDING",
                )
                jobRepository.saveJobAndReduceStock(job)
                _selectedParts.value = emptyList() 
                onResult(true, "Job saved! Invoice: $invoiceNo")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }
}
