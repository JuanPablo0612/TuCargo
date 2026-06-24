package com.juanpablo0612.tucargo.domain.model

data class WalletBalance(
    val commissionOwed: Cop,
    val ivaOwed: Cop,
    val ssOwed: Cop,
    val totalOwed: Cop
)
