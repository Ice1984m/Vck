package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_certificates")
data class ApiCertificate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,             // e.g., "KBC Developer Sandbox" or "PSD2 QWAC Certificate"
    val endpointUrl: String,      // e.g., "https://api.kbc.be/v1/sandbox"
    val clientId: String,         // e.g., "kbc-client-9925"
    val certificateType: String,  // e.g., "QWAC", "QSealC", "API Key"
    val certificateContent: String, // PEM or Private Key mock content
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
