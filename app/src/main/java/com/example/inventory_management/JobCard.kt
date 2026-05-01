package com.example.inventory_management

import com.google.firebase.firestore.DocumentId
import java.util.Date

// Represents a part used in a repair job
data class UsedPart(
    val partId: String = "",
    val partName: String = "",
    val quantityUsed: Int = 0,
    val priceAtTime: Double = 0.0,
)

// Represents the full Job Card
data class JobCard(
    @DocumentId
    val jobId: String = "",
    val invoiceNumber: String = "",
    val vehicleNumber: String = "",
    val customerName: String = "",
    val workDetails: String = "",
    val date: Date = Date(),
    val dueDate: Date? = null,
    val partsUsed: List<UsedPart> = emptyList(),
    val laborCharge: Double = 0.0,
    val gstPercentage: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentStatus: String = "UNPAID", // "PAID", "PARTIAL", "UNPAID"
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "COMPLETED", "DELIVERED"
) {
    companion object {
        fun calculateStatus(total: Double, paid: Double): String {
            return when {
                (paid >= total) && (total > 0) -> "PAID"
                paid > 0 -> "PARTIAL"
                else -> "UNPAID"
            }
        }
    }
}
