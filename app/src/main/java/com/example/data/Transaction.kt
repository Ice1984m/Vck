package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: String,
    val type: String,          // "DEBIT" (Payment) or "CREDIT" (Received)
    val amount: Double,
    val recipientName: String,
    val recipientIban: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Voltooid" // "Voltooid" (Completed), "In Behandeling" (Pending), "Geweigerd" (Rejected)
)
