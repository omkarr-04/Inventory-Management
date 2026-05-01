package com.example.inventory_management

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    /**
     * Formats a Double amount into Indian Rupee (INR) currency format.
     * Example: 1000.0 -> ₹1,000.00
     */
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        return format.format(amount)
    }
}
