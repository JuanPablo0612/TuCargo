package com.juanpablo0612.tucargo.domain.model

data class PricingConfig(
    val baseFare: Cop,
    val perKmFare: Cop,
    val commissionRate: Int
)
