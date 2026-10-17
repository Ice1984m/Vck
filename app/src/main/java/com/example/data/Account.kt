package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String, // e.g., "zichtrekening"
    val accountName: String,     // e.g., "KBC Plusrekening"
    val iban: String,            // e.g., "BE96 1234 5678 9012"
    val balance: Double,         // e.g., 1450.75
    val currency: String = "EUR",
    val holderName: String = "Math Moors"
)
