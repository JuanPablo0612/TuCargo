package com.juanpablo0612.tucargo.domain.model

data class WalletConfig(
    val maxCommissionDebt: Cop,
    val warningThreshold1: Cop,
    val warningThreshold2: Cop,
    val settlementCadenceDays: Int,
    val ivaRate: Double,
    val ssDriverShare: Double
)
