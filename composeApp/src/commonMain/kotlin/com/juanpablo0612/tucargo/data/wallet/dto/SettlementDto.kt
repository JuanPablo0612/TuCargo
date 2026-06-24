package com.juanpablo0612.tucargo.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettlementDto(
    @SerialName("driver_id")
    val driverId: String = "",
    @SerialName("amount_paid")
    val amountPaid: Int = 0,
    @SerialName("gateway_reference")
    val gatewayReference: String = "",
    @SerialName("status")
    val status: String = "PENDING",
    @SerialName("invoice_id")
    val invoiceId: String? = null,
    @SerialName("timestamp")
    val timestamp: String = "" // ISO 8601 string as per JSON
)
