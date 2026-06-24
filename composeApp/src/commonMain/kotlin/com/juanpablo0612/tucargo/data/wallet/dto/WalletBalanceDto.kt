package com.juanpablo0612.tucargo.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceDto(
    @SerialName("commission_owed")
    val commissionOwed: Int = 0,
    @SerialName("iva_owed")
    val ivaOwed: Int = 0,
    @SerialName("ss_owed")
    val ssOwed: Int = 0,
    @SerialName("total_owed")
    val totalOwed: Int = 0
)
