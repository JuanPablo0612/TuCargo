package com.juanpablo0612.tucargo.domain.model

data class Settlement(
    val id: String,
    val driverId: String,
    val amountPaid: Cop,
    val gatewayReference: String,
    val status: SettlementStatus,
    val invoiceId: String?,
    val timestamp: Long
)
