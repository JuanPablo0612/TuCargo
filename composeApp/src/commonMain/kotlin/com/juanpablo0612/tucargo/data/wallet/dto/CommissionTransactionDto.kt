package com.juanpablo0612.tucargo.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommissionTransactionDto(
    @SerialName("type")
    val type: String = "COMMISSION_ACCRUAL",
    @SerialName("trip_id")
    val tripId: String? = null,
    @SerialName("amount_cop")
    val amountCop: Int = 0,
    @SerialName("timestamp")
    val timestamp: String = "", // ISO 8601 string as per JSON
    @SerialName("description")
    val description: String = ""
)
