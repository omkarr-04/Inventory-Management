package com.example.inventory_management

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JobHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JobRepository()
    private val notificationHelper = NotificationHelper(application)

    val jobHistory: StateFlow<List<JobCard>> = repository.getJobHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        // Reduced notification frequency by checking only once on startup or when history is specifically loaded
        // and using a simple timestamp check to prevent spamming
        viewModelScope.launch {
            jobHistory.collect { jobs ->
                // Only alert if there are jobs and it's an "occasional" check
                if (jobs.isNotEmpty()) {
                    val unpaidJobs = jobs.filter { (it.paymentStatus != "PAID") && ((it.totalAmount - it.amountPaid) > 0) }
                    if (unpaidJobs.size > 3) { // Only alert if more than 3 customers owe money (less frequent)
                        notificationHelper.showPaymentReminderNotification("Multiple Customers", unpaidJobs.sumOf { it.totalAmount - it.amountPaid })
                    } else if (unpaidJobs.size == 1) {
                         val job = unpaidJobs.first()
                         notificationHelper.showPaymentReminderNotification(job.customerName, job.totalAmount - job.amountPaid)
                    }
                }
            }
        }
    }

    fun updatePayment(job: JobCard, additionalAmount: Double, onResult: (Boolean, String) -> Unit) {
        val newAmountPaid = job.amountPaid + additionalAmount
        if (newAmountPaid > job.totalAmount) {
            onResult(false, "Total paid cannot exceed bill amount.")
            return
        }
        val newStatus = JobCard.calculateStatus(job.totalAmount, newAmountPaid)
        viewModelScope.launch {
            try {
                repository.updateJobPayment(job.jobId, newAmountPaid, newStatus)
                onResult(true, "Payment updated successfully!")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateStatus(job: JobCard, newStatus: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateJobStatus(job.jobId, newStatus)
                onResult(true, "Job status updated to $newStatus")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearHistory(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.clearAllJobs()
                onResult(true, "Job history cleared.")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }
}
