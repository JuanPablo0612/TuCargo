package com.juanpablo0612.tucargo.data.wallet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletTransactionDto(
    val id: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    @SerialName("reference_id")
    val referenceId: String = "",
    val description: String = "",
    val status: String = "COMPLETED"
)
