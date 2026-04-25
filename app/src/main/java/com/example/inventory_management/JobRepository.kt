package com.example.inventory_management

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JobRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val jobsCollection = firestore.collection("jobs")
    private val partsCollection = firestore.collection("parts")

    // FETCH: Real-time job history, sorted by newest first
    fun getJobHistory(): Flow<List<JobCard>> {
        return jobsCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(JobCard::class.java)
            }
    }

    // SAVE: Submit job and reduce stock
    suspend fun saveJobAndReduceStock(job: JobCard) {
        firestore.runTransaction { transaction ->
            // Step 1: Perform ALL reads first
            val partUpdates = job.partsUsed.map { usedPart ->
                val partRef = partsCollection.document(usedPart.partId)
                val snapshot = transaction.get(partRef) // READ
                
                val currentQty = snapshot.getLong("quantity") ?: 0L
                val newQty = currentQty - usedPart.quantityUsed
                
                if (newQty < 0) {
                    throw Exception("Not enough stock for ${usedPart.partName}")
                }
                
                // Return a pair of the reference and the new quantity to update later
                partRef to newQty
            }

            // Step 2: Perform ALL writes after all reads are done
            for ((ref, newQty) in partUpdates) {
                transaction.update(ref, "quantity", newQty) // WRITE
            }
            
            val newJobRef = jobsCollection.document()
            transaction.set(newJobRef, job) // WRITE
        }.await()
    }

    /**
     * Updates only the payment information for a specific job.
     */
    suspend fun updateJobPayment(jobId: String, amountPaid: Double, status: String) {
        jobsCollection.document(jobId).update(
            "amountPaid", amountPaid,
            "paymentStatus", status
        ).await()
    }

    /**
     * Updates only the work status for a specific job.
     */
    suspend fun updateJobStatus(jobId: String, newStatus: String) {
        jobsCollection.document(jobId).update("status", newStatus).await()
    }

    /**
     * Deletes all jobs from the collection.
     */
    suspend fun clearAllJobs() {
        val snapshot = jobsCollection.get().await()
        firestore.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()
    }
}
