package com.juanpablo0612.tucargo.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletConfigDto(
    @SerialName("max_commission_debt")
    val maxCommissionDebt: Int = 100000,
    @SerialName("warning_threshold_1")
    val warningThreshold1: Int = 80000,
    @SerialName("warning_threshold_2")
    val warningThreshold2: Int = 90000,
    @SerialName("settlement_cadence_days")
    val settlementCadenceDays: Int = 7,
    @SerialName("iva_rate")
    val ivaRate: Double = 0.19,
    @SerialName("ss_driver_share")
    val ssDriverShare: Double = 0.40
)
