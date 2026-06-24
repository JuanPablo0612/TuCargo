package com.juanpablo0612.tucargo.domain.model

data class CommissionTransaction(
    val id: String,
    val type: CommissionTransactionType,
    val tripId: String?,
    val amount: Cop,
    val timestamp: Long, // Epoch milliseconds
    val description: String
)
